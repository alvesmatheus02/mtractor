package br.com.sankhya.bhz.central.acoes;

import br.com.sankhya.bhz.utils.ErroUtils;
import br.com.sankhya.extensions.actionbutton.AcaoRotinaJava;
import br.com.sankhya.extensions.actionbutton.ContextoAcao;
import br.com.sankhya.extensions.actionbutton.Registro;
import br.com.sankhya.jape.EntityFacade;
import br.com.sankhya.jape.dao.JdbcWrapper;
import br.com.sankhya.jape.sql.NativeSql;
import br.com.sankhya.jape.vo.DynamicVO;
import br.com.sankhya.jape.wrapper.JapeFactory;
import br.com.sankhya.jape.wrapper.JapeWrapper;
import br.com.sankhya.modelcore.auth.AuthenticationInfo;
import br.com.sankhya.modelcore.util.DynamicEntityNames;
import br.com.sankhya.modelcore.util.EntityFacadeFactory;
import com.sankhya.util.TimeUtils;

import java.math.BigDecimal;
import java.sql.ResultSet;

public class acaoValAltPedConf implements AcaoRotinaJava {
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
        DynamicVO lib1001VO = libDAO.findOne("NUCHAVE = ? AND EVENTO = 1001 AND TABELA =  'TGFCAB'", nuNota);


        boolean confirmada = cabVO.asString("STATUSNOTA").equals("L");
        boolean pedVenda = cabVO.asString("TIPMOV").equals("P");
        BigDecimal vlrNota = cabVO.asBigDecimalOrZero("VLRNOTA");
        BigDecimal codParc = cabVO.asBigDecimalOrZero("CODPARC");
        BigDecimal vlrLimite = BigDecimal.ZERO;
        BigDecimal vlrLimDisp = BigDecimal.ZERO;
        BigDecimal vlrLiberacao = BigDecimal.ZERO;
        String msgRetorno = "SEM MENSAGEM DE RETORNO, REVISAR CÓDIGO.";


        if (null != libVO && pedVenda && confirmada) {

            if (null == lib1001VO && libVO.asString("REPROVADO").equals("N") && null != libVO.asTimestamp("DHLIB") && libVO.asBigDecimalOrZero("VLRLIBERADO").compareTo(BigDecimal.ZERO) > 0) {

                EntityFacade dwfEntityFacade = EntityFacadeFactory.getDWFFacade();
                JdbcWrapper jdbc = dwfEntityFacade.getJdbcWrapper();
                NativeSql sql = new NativeSql(jdbc);

                sql.loadSql(acaoValAltPedConf.class, "sql/buscaVlrCredParc.sql");
                sql.setNamedParameter("CODPARC", codParc);
                sql.setNamedParameter("VLRNOTA", vlrNota);
                ResultSet resultSet = sql.executeQuery();

                if (resultSet.next()) {
                    vlrLimite = resultSet.getBigDecimal("LIMITE");
                    vlrLimDisp = resultSet.getBigDecimal("LIMITEDISP");
                } else {
                    ErroUtils.disparaErro("Não foi possível revisar limite de crédito do parceiro, favor revisar cadastro de limite.");
                }

                if (vlrLimDisp.compareTo(BigDecimal.ZERO) < 0) {
                    vlrLiberacao = vlrLimDisp.abs().add(vlrLimite);

                    if (vlrLiberacao.compareTo(libVO.asBigDecimalOrZero("VLRLIBERADO")) > 0) {
                        libDAO.create()
                                .set("NUCHAVE", nuNota)
                                .set("SEQUENCIA", BigDecimal.ONE)
                                .set("TABELA", "TGFCAB")
                                .set("EVENTO", BigDecimal.valueOf(1001))
                                .set("CODUSUSOLICIT", AuthenticationInfo.getCurrent().getUserID())
                                .set("DHSOLICIT", TimeUtils.getNow())
                                .set("VLRATUAL", vlrLimDisp.abs().add(vlrLimite))
                                .set("CODCENCUS", cabVO.asBigDecimalOrZero("CODCENCUS"))
                                .set("CODUSULIB", BigDecimal.ZERO)
                                .set("OBSERVACAO", "Alteração pedido após confirmação do pedido, valor limite liberado de R$" + libVO.asBigDecimalOrZero("VLRLIBERADO") + ", novo valor solicitado de R$" + vlrNota + ".")
                                .set("VLRLIMITE", BigDecimal.ZERO)
                                .save();

                        linha.setCampo("AD_PEDALTPOSCONF","S");

                        msgRetorno = "Solicitação de liberação de limite pós confirmação solicitada com sucesso!";
                    } else {
                        msgRetorno = "Pedido liberado para conferência, parceiro com limite disponível dentro da liberação.";

                    }
                } else {
                    msgRetorno = "O parceiro possui limite de crédito suficiente, não sendo necessária a liberação adicional de limite de crédito.";
                }

            } else if (null != lib1001VO) {
                if (lib1001VO.asString("REPROVADO").equals("S") && null != lib1001VO.asTimestamp("DHLIB")) {
                    ErroUtils.disparaErro("Liberação de limite de crédito após a confirmação recusada. Favor consultar a liberação existente e revisar a solicitação.");
                } else if (lib1001VO.asString("REPROVADO").equals("N") && null == lib1001VO.asTimestamp("DHLIB") && lib1001VO.asBigDecimalOrZero("VLRLIBERADO").compareTo(BigDecimal.ZERO) == 0) {
                    ErroUtils.disparaErro("O pedido já possui solicitação de liberação de limite de crédito em andamento. Favor aguardar a análise.");
                } else {
                    msgRetorno = "Liberação de crédito após a confirmação realizada com sucesso.";
                }
            }
        }
        contexto.setMensagemRetorno(msgRetorno);
    }
}
