package br.com.sankhya.bhz.central.regras;

import br.com.sankhya.bhz.utils.AcessoBanco;
import br.com.sankhya.bhz.utils.ErroUtils;
import br.com.sankhya.jape.vo.DynamicVO;
import br.com.sankhya.jape.wrapper.JapeFactory;
import br.com.sankhya.jape.wrapper.JapeWrapper;
import br.com.sankhya.modelcore.comercial.ContextoRegra;
import br.com.sankhya.modelcore.comercial.LimiteCreditoHelpper;
import br.com.sankhya.modelcore.comercial.Regra;
import br.com.sankhya.modelcore.util.DynamicEntityNames;

import java.math.BigDecimal;
import java.sql.Timestamp;

public class regraValAltPedVenda implements Regra {
    JapeWrapper cabConfDAO = JapeFactory.dao("CabecalhoConferencia");
    JapeWrapper cabDAO = JapeFactory.dao(DynamicEntityNames.CABECALHO_NOTA);
    JapeWrapper tpvDAO = JapeFactory.dao(DynamicEntityNames.TIPO_NEGOCIACAO);
    JapeWrapper libDAO = JapeFactory.dao(DynamicEntityNames.LIBERACAO_LIMITE);

    @Override
    public void beforeInsert(ContextoRegra ctx) throws Exception {

    }

    @Override
    public void beforeUpdate(ContextoRegra ctx) throws Exception {
        DynamicVO vo = ctx.getPrePersistEntityState().getNewVO();
        DynamicVO voOld = ctx.getPrePersistEntityState().getOldVO();

        BigDecimal nuNota = vo.asBigDecimalOrZero("NUNOTA");

        DynamicVO cabVO = cabDAO.findByPK(nuNota);
        DynamicVO cabConfVO = cabConfDAO.findOne("NUNOTAORIG = ?", nuNota);

        boolean tgfIte = "ItemNota".equals(ctx.getPrePersistEntityState().getDao().getEntityName());
        boolean tgfCab = "CabecalhoNota".equals(ctx.getPrePersistEntityState().getDao().getEntityName());
        boolean pedido = cabVO.asString("TIPMOV").equals("P");
        boolean confirmado = cabVO.asString("STATUSNOTA").equals("L");
        boolean altTipVenda = false;
        boolean altVlrNota = false;
        boolean altParc = false;
        BigDecimal codTipVenda = null;
        BigDecimal codParc = null;
        BigDecimal vlrNota = null;
        BigDecimal vlNotaOld = null;
        Timestamp dhTipVenda = null;


        if (tgfIte && pedido && confirmado) {
            if (null != cabConfVO) {
                ErroUtils.disparaErro("Alterações no pedido não são permitidas após o início do processo de conferência. Solicito, por gentileza, alinhar com o setor responsável.");
            } else {
                AcessoBanco acessoBanco = new AcessoBanco();
                acessoBanco.openSession();
                acessoBanco.update("UPDATE TGFCAB SET AD_PEDALTPOSCONF = 'A' WHERE NUNOTA = ?", nuNota);
                acessoBanco.closeSession();
            }
        } else if (tgfCab && pedido && confirmado) {
            altVlrNota = ctx.getPrePersistEntityState().getModifingFields().isModifing("VLRNOTA");
            altTipVenda = ctx.getPrePersistEntityState().getModifingFields().isModifing("CODTIPVENDA");
            altParc = ctx.getPrePersistEntityState().getModifingFields().isModifing("CODPARC");
            codTipVenda = vo.asBigDecimalOrZero("CODTIPVENDA");
            dhTipVenda = vo.asTimestamp("DHTIPVENDA");
            codParc = vo.asBigDecimalOrZero("CODPARC");
            vlrNota =  vo.asBigDecimalOrZero("VLRNOTA");
            vlNotaOld = voOld.asBigDecimalOrZero("VLRNOTA");

            DynamicVO tpvVO = tpvDAO.findByPK(codTipVenda, dhTipVenda);

            LimiteCreditoHelpper limiteCreditoHelpper = new LimiteCreditoHelpper();
            AcessoBanco acessoBanco = new AcessoBanco();
            acessoBanco.openSession();

            if (altParc || (null != tpvVO && altTipVenda && (tpvVO.asString("SUBTIPOVENDA").equals("2") || tpvVO.asString("SUBTIPOVENDA").equals("3")))) {
                DynamicVO libVO = libDAO.findOne("NUCHAVE = ? AND EVENTO = 3 AND TABELA =  'TGFCAB'", nuNota);

                BigDecimal vlrLiberacao = null != libVO ? libVO.asBigDecimalOrZero("VLRLIBERADO") : BigDecimal.ZERO;
                BigDecimal vlrLimite = BigDecimal.ZERO;

                if (null != libVO && libVO.asString("REPROVADO").equals("N") && null != libVO.asTimestamp("DHLIB") && vlrLiberacao.compareTo(BigDecimal.ZERO) > 0) {
                    vlrLimite = BigDecimal.valueOf(limiteCreditoHelpper.getLimiteNegociado(codParc, nuNota)).add(vlrNota);

                    if (vlrLimite.abs().compareTo(vlrLiberacao) > 0) {
                        limiteCreditoHelpper.validarLimiteCredito(nuNota);

                        acessoBanco.update("UPDATE TSILIB SET DHLIB = NULL, VLRLIBERADO = 0, CODUSULIB = 0 WHERE NUCHAVE = ? AND EVENTO = 3 AND TABELA = 'TGFCAB'", nuNota);
                        acessoBanco.update("UPDATE TGFCAB SET AD_PEDALTPOSCONF = 'P' WHERE NUNOTA = ?", nuNota);
                        acessoBanco.closeSession();
                    }

                } else if (null != libVO && libVO.asString("REPROVADO").equals("S") && null != libVO.asTimestamp("DHLIB")) {
                    acessoBanco.update("DELETE TSILIB WHERE NUCHAVE = ? AND EVENTO = 3 AND TABELA = 'TGFCAB'", nuNota);
                    acessoBanco.update("UPDATE TGFCAB SET AD_PEDALTPOSCONF = 'P' WHERE NUNOTA = ?", nuNota);
                    acessoBanco.closeSession();

                    limiteCreditoHelpper.validarLimiteCredito(nuNota);

                } else if (null != libVO && libVO.asString("REPROVADO").equals("N") && null == libVO.asTimestamp("DHLIB") && vlrLiberacao.compareTo(BigDecimal.ZERO) == 0) {
                    limiteCreditoHelpper.validarLimiteCredito(nuNota);

                    acessoBanco.update("UPDATE TGFCAB SET AD_PEDALTPOSCONF = 'P' WHERE NUNOTA = ?", nuNota);
                    acessoBanco.closeSession();

                } else {
                    limiteCreditoHelpper.validarLimiteCredito(nuNota);

                    DynamicVO libNovoVO = libDAO.findOne("NUCHAVE = ? AND EVENTO = 3 AND TABELA =  'TGFCAB'", nuNota);

                    if (null != libNovoVO) {
                        acessoBanco.update("UPDATE TGFCAB SET AD_PEDALTPOSCONF = 'P' WHERE NUNOTA = ?", nuNota);
                        acessoBanco.closeSession();
                    }
                }

            }

            if (null != cabConfVO && (!vlNotaOld.equals(vlrNota) || altVlrNota)) {
                ErroUtils.disparaErro("Alterações no pedido não são permitidas após o início do processo de conferência. Solicito, por gentileza, alinhar com o setor responsável.");
            }
        }
    }

    @Override
    public void beforeDelete(ContextoRegra ctx) throws Exception {

    }

    @Override
    public void afterInsert(ContextoRegra ctx) throws Exception {

    }

    @Override
    public void afterUpdate(ContextoRegra ctx) throws Exception {

    }

    @Override
    public void afterDelete(ContextoRegra ctx) throws Exception {

    }
}
