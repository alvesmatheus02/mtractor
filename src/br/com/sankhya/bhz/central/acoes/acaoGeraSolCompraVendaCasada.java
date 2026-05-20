package br.com.sankhya.bhz.central.acoes;

import br.com.sankhya.bhz.utils.ErroUtils;
import br.com.sankhya.bhz.utils.Utilitarios;
import br.com.sankhya.extensions.actionbutton.AcaoRotinaJava;
import br.com.sankhya.extensions.actionbutton.ContextoAcao;
import br.com.sankhya.extensions.actionbutton.Registro;
import br.com.sankhya.jape.vo.DynamicVO;
import br.com.sankhya.jape.wrapper.JapeFactory;
import br.com.sankhya.jape.wrapper.JapeWrapper;
import br.com.sankhya.jape.wrapper.fluid.FluidCreateVO;
import br.com.sankhya.modelcore.auth.AuthenticationInfo;
import br.com.sankhya.modelcore.util.DynamicEntityNames;
import com.sankhya.util.TimeUtils;

import java.math.BigDecimal;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

public class acaoGeraSolCompraVendaCasada implements AcaoRotinaJava {
    JapeWrapper cabDAO = JapeFactory.dao(DynamicEntityNames.CABECALHO_NOTA);
    JapeWrapper iteDAO = JapeFactory.dao(DynamicEntityNames.ITEM_NOTA);
    JapeWrapper varDAO = JapeFactory.dao(DynamicEntityNames.COMPRA_VENDA_VARIOS_PEDIDO);
    JapeWrapper tpoDAO = JapeFactory.dao(DynamicEntityNames.TIPO_OPERACAO);
    JapeWrapper parDAO = JapeFactory.dao(DynamicEntityNames.PARCEIRO);
    JapeWrapper movConsDAO = JapeFactory.dao("AD_BHZCONTMOVCONSIG");

    @Override
    public void doAction(ContextoAcao contexto) throws Exception {
        Registro[] linhas = contexto.getLinhas();

        if (linhas.length < 1){
            ErroUtils.disparaErro("Selecione ao menos uma linha para ação!");
        }
        DynamicVO tpoVO = tpoDAO.findByPK(BigDecimal.valueOf(1314), Utilitarios.getDataMaxTipoOper(BigDecimal.valueOf(1314)));
        DynamicVO cabModVO = cabDAO.findByPK(BigDecimal.valueOf(80722));

        BigDecimal nuNota = BigDecimal.ZERO;
        BigDecimal nuNotaMov = BigDecimal.ZERO;
        BigDecimal seqMov = BigDecimal.ZERO;

        DynamicVO cabMov = null;

        for(Registro linha : linhas) {
            nuNota = new BigDecimal(linha.getCampo("NUNOTA").toString());

            DynamicVO cabVO = cabDAO.findByPK(nuNota);
            DynamicVO parVO = parDAO.findByPK(cabVO.asBigDecimalOrZero("AD_CODPARCFORC"));
            Collection<DynamicVO> itens = iteDAO.find("NUNOTA = ? AND NULLVALUE(PENDENTE,'N') = 'S'", nuNota);

            if (!cabVO.asBigDecimalOrZero("CODTIPOPER").equals(BigDecimal.valueOf(1014))) {
                ErroUtils.disparaErro("Movimento selecionado não é uma solicitação de venda casada, favor revisar seleção!");
            }
            if (!cabVO.asString("STATUSNOTA").equals("L")) {
                ErroUtils.disparaErro("Movimento selecionado não está confirmado, favor revisar seleção!");
            }
            if (!cabVO.asString("PENDENTE").equals("S")) {
                ErroUtils.disparaErro("Solicitação de venda casada está pendente, favor revisar lançamento.");
            }
            if (null != parVO) {
                if (null != parVO.asString("FORNECEDOR") && !parVO.asString("FORNECEDOR").equals("S")) {
                    ErroUtils.disparaErro("Parceiro fornecedor informado não é fornecedor, favor verificar!");
                }
            } else {
                ErroUtils.disparaErro("Parceiro fornecedor não encontrado, favor verificar!");
            }

            Map<String, Object> alteracoes = new HashMap<>();
            alteracoes.put("DHTIPOPER", Utilitarios.getDataMaxTipoOper(BigDecimal.valueOf(1314)));
            alteracoes.put("CODPARC", cabVO.asBigDecimalOrZero("AD_CODPARCFORC"));
            alteracoes.put("AD_CODPARCCLIENTE", cabVO.asBigDecimalOrZero("CODPARC"));
            alteracoes.put("CODNAT", cabVO.asBigDecimalOrZero("CODNAT"));
            alteracoes.put("CODCENCUS", cabVO.asBigDecimalOrZero("CODCENCUS"));
            alteracoes.put("CODTIPVENDA", cabVO.asBigDecimalOrZero("CODTIPVENDA"));
            alteracoes.put("DHTIPVENDA", cabVO.asTimestamp("DHTIPVENDA"));
            alteracoes.put("AD_CODVENDSOL", cabVO.asBigDecimalOrZero("CODVEND"));

            cabMov = Utilitarios.duplicaRegistroVO(cabModVO, "CabecalhoNota", alteracoes);

            nuNotaMov = cabMov.asBigDecimalOrZero("NUNOTA");

            if (itens.isEmpty()) {
                ErroUtils.disparaErro("Solicitação de venda casada não possui itens pendentes, favor revisar lançamento.");
            }

            for (DynamicVO iteVO : itens) {
                seqMov = insertItens(cabMov, iteVO, tpoVO);
                geraVar(nuNotaMov, seqMov, nuNota, iteVO.asBigDecimalOrZero("SEQUENCIA"), iteVO.asBigDecimalOrZero("QTDNEG"));
            }
        }

        contexto.setMensagemRetorno("Pedido de compra casada gerada com sucesso! <br> Nro. Único: "+nuNotaMov);

    }

    private static BigDecimal insertItens (DynamicVO cabMov, DynamicVO iteVO, DynamicVO tpoVO) throws Exception {
        JapeWrapper iteDAO = JapeFactory.dao(DynamicEntityNames.ITEM_NOTA);

        BigDecimal atualEst = BigDecimal.ZERO;
        BigDecimal nuNotaMov = cabMov.asBigDecimalOrZero("NUNOTA");
        BigDecimal sequencia = null;
        BigDecimal codEmp = cabMov.asBigDecimalOrZero("CODEMP");
        BigDecimal codProd = iteVO.asBigDecimalOrZero("CODPROD");
        BigDecimal qtdNeg = iteVO.asBigDecimalOrZero("QTDNEG");
        BigDecimal vlrUnit = iteVO.asBigDecimalOrZero("VLRUNIT");
        BigDecimal vlrTot = iteVO.asBigDecimalOrZero("VLRTOT");
        BigDecimal codLocalOrig = iteVO.asBigDecimalOrZero("CODLOCALORIG");
        BigDecimal vlrCustoCompra = iteVO.asBigDecimalOrZero("AD_VLRCUSTOCOMPRA");
        BigDecimal vlrPrecoVenda = iteVO.asBigDecimalOrZero("AD_VLRPRECOVENDA");

        String controle = iteVO.asString("CONTROLE");
        String codVol = iteVO.asString("CODVOL");
        String atualEstConfTop = tpoVO.asString("ADIARATUALEST");
        String atualEstTop = tpoVO.asString("ATUALEST");

        String teceiros = "N";
        String reserva = "N";

        if (atualEstTop.equals("B") && atualEstConfTop.equals("N")) {
            atualEst = BigDecimal.valueOf(-1);
        } else if (atualEstTop.equals("E") && atualEstConfTop.equals("N")) {
            atualEst = BigDecimal.ONE;
        } else if (atualEstTop.equals("R") && atualEstConfTop.equals("N")) {
            atualEst = BigDecimal.ONE;
            reserva = "S";
        }

        FluidCreateVO creITE = JapeFactory.dao(DynamicEntityNames.ITEM_NOTA).create();
        creITE.set("NUNOTA", nuNotaMov);
        creITE.set("CODEMP", codEmp);
        creITE.set("CODPROD", codProd);
        creITE.set("CODVOL", codVol);
        creITE.set("QTDNEG", qtdNeg);
        creITE.set("CONTROLE", controle);
        creITE.set("ATUALESTOQUE", atualEst);
        creITE.set("RESERVA", reserva);
        creITE.set("CODLOCALORIG", codLocalOrig);
        creITE.set("TERCEIROS", teceiros);
        creITE.set("VLRUNIT", vlrUnit);
        creITE.set("VLRTOT", vlrTot);
        creITE.set("AD_VLRCUSTOCOMPRA", vlrCustoCompra);
        creITE.set("AD_VLRPRECOVENDA", vlrPrecoVenda);

        DynamicVO itemCriado = creITE.save();

        sequencia = itemCriado.asBigDecimalOrZero("SEQUENCIA");

        return sequencia;
    }

    private static void geraVar(BigDecimal nunota, BigDecimal sequencia, BigDecimal nunotaorig, BigDecimal sequenciaorig, BigDecimal qtdatendida) throws Exception {

        JapeWrapper varDAO = JapeFactory.dao("CompraVendavariosPedido");

        FluidCreateVO varVO = varDAO.create();
        varVO.set("NUNOTA",nunota);
        varVO.set("SEQUENCIA",sequencia);
        varVO.set("NUNOTAORIG",nunotaorig);
        varVO.set("SEQUENCIAORIG",sequenciaorig);
        varVO.set("QTDATENDIDA", qtdatendida);
        varVO.set("STATUSNOTA","A");
        varVO.set("CUSATEND", null);
        varVO.set("FIXACAO", null);
        varVO.set("NROATOCONCDRAW", null);
        varVO.set("NROMEMORANDO", null);
        varVO.set("NROREGEXPORT", null);
        varVO.set("ORDEMPROD", null);
        varVO.save();
    }


}
