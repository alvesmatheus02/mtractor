package br.com.sankhya.bhz.central.acoes;

import br.com.sankhya.bhz.utils.AcessoBanco;
import br.com.sankhya.bhz.utils.ErroUtils;
import br.com.sankhya.extensions.actionbutton.AcaoRotinaJava;
import br.com.sankhya.extensions.actionbutton.ContextoAcao;
import br.com.sankhya.extensions.actionbutton.Registro;
import br.com.sankhya.jape.vo.DynamicVO;
import br.com.sankhya.jape.wrapper.JapeFactory;
import br.com.sankhya.jape.wrapper.JapeWrapper;
import br.com.sankhya.modelcore.comercial.LimiteCreditoHelpper;
import br.com.sankhya.modelcore.comercial.regras.LimiteCredito;
import br.com.sankhya.modelcore.util.DynamicEntityNames;

import java.math.BigDecimal;

public class acaoValAltPedConfNovo implements AcaoRotinaJava {
    JapeWrapper libDAO = JapeFactory.dao(DynamicEntityNames.LIBERACAO_LIMITE);
    JapeWrapper cabDAO = JapeFactory.dao(DynamicEntityNames.CABECALHO_NOTA);

    @Override
    public void doAction(ContextoAcao contexto) throws Exception {
        Registro[] linhas = contexto.getLinhas();

        if (linhas.length > 1) {
            contexto.mostraErro("Selecione apenas 1 registro");
        }

        Registro linha = linhas[0];

        BigDecimal nuNota = (BigDecimal) linha.getCampo("NUNOTA");

        DynamicVO cabVO = cabDAO.findByPK(nuNota);
        DynamicVO libVO = libDAO.findOne("NUCHAVE = ? AND EVENTO = 3 AND TABELA =  'TGFCAB'", nuNota);

        boolean confirmada = cabVO.asString("STATUSNOTA").equals("L");
        boolean pedVenda = cabVO.asString("TIPMOV").equals("P");
        BigDecimal vlrLimite = BigDecimal.ZERO;
        BigDecimal vlrLiberacao = null != libVO ? libVO.asBigDecimalOrZero("VLRLIBERADO") : BigDecimal.ZERO;
        BigDecimal vlrNota = cabVO.asBigDecimalOrZero("VLRNOTA");
        BigDecimal codParc = cabVO.asBigDecimalOrZero("CODPARC");

        String msgRetorno = "SEM MENSAGEM DE RETORNO, REVISAR CÓDIGO.";
        LimiteCreditoHelpper limiteCreditoHelpper = new LimiteCreditoHelpper();

        if (null != libVO && pedVenda && confirmada && libVO.asString("REPROVADO").equals("N") && null != libVO.asTimestamp("DHLIB") && vlrLiberacao.compareTo(BigDecimal.ZERO) > 0) {
            vlrLimite = BigDecimal.valueOf(limiteCreditoHelpper.getLimiteNegociado(codParc, nuNota)).add(vlrNota);

            if (vlrLimite.abs().compareTo(vlrLiberacao) > 0) {
                limiteCreditoHelpper.validarLimiteCredito(nuNota);

                AcessoBanco acessoBanco = new AcessoBanco();
                acessoBanco.openSession();
                acessoBanco.update("UPDATE TSILIB SET DHLIB = NULL, VLRLIBERADO = 0, CODUSULIB = 0 WHERE NUCHAVE = ? AND EVENTO = 3 AND TABELA = 'TGFCAB'", nuNota);
                acessoBanco.closeSession();

                linha.setCampo("AD_PEDALTPOSCONF", "P");
                msgRetorno = "Gerado nova liberação de limites após confirmação do pedido.";
            } else {
                linha.setCampo("AD_PEDALTPOSCONF", "LR");
                msgRetorno = "O parceiro possui limite de crédito suficiente, não sendo necessária a liberação adicional de limite de crédito.";
            }

        } else if (null != libVO && pedVenda && confirmada && libVO.asString("REPROVADO").equals("S") && null != libVO.asTimestamp("DHLIB")) {

            contexto.confirmar("Limite de Crédito Reprovado.","Deseja gerar uma nova solicitação de liberação?",1);

            AcessoBanco acessoBanco = new AcessoBanco();
            acessoBanco.openSession();
            acessoBanco.update("DELETE TSILIB WHERE NUCHAVE = ? AND EVENTO = 3 AND TABELA = 'TGFCAB'", nuNota);
            acessoBanco.closeSession();

            limiteCreditoHelpper.validarLimiteCredito(nuNota);

            linha.setCampo("AD_PEDALTPOSCONF", "P");
            msgRetorno = "Gerado liberação de limites após confirmação do pedido.";
        } else if (null != libVO && pedVenda && confirmada && libVO.asString("REPROVADO").equals("N") && null == libVO.asTimestamp("DHLIB") && vlrLiberacao.compareTo(BigDecimal.ZERO) == 0) {
            msgRetorno = "O pedido já possui solicitação de liberação de limite de crédito em andamento. Favor aguardar a análise.";
        } else {
            limiteCreditoHelpper.validarLimiteCredito(nuNota);

            DynamicVO libNovoVO = libDAO.findOne("NUCHAVE = ? AND EVENTO = 3 AND TABELA =  'TGFCAB'", nuNota);

            if (null != libNovoVO) {
                linha.setCampo("AD_PEDALTPOSCONF", "P");
                msgRetorno = "Gerado liberação de limites após confirmação do pedido.";
            } else {
                linha.setCampo("AD_PEDALTPOSCONF", "LR");
                msgRetorno = "O parceiro possui limite de crédito suficiente, não sendo necessária a liberação adicional de limite de crédito.";
            }
        }
        contexto.setMensagemRetorno(msgRetorno);
    }
}
