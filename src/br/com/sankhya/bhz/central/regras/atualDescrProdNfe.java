package br.com.sankhya.bhz.central.regras;

import br.com.sankhya.bhz.utils.AcessoBanco;
import br.com.sankhya.jape.core.JapeSession;
import br.com.sankhya.jape.vo.DynamicVO;
import br.com.sankhya.jape.wrapper.JapeFactory;
import br.com.sankhya.jape.wrapper.JapeWrapper;
import br.com.sankhya.modelcore.comercial.ContextoRegra;
import br.com.sankhya.modelcore.comercial.Regra;

import java.util.Collection;

public class atualDescrProdNfe implements Regra {
    JapeWrapper cabDAO = JapeFactory.dao("CabecalhoNota");
    JapeWrapper iteDAO = JapeFactory.dao("ItemNota");
    JapeWrapper proDAO = JapeFactory.dao("Produto");
    JapeWrapper tpoDAO = JapeFactory.dao("TipoOperacao");
    @Override
    public void beforeInsert(ContextoRegra contextoRegra) throws Exception {

    }

    @Override
    public void beforeUpdate(ContextoRegra contextoRegra) throws Exception {
        boolean tgfCab = "CabecalhoNota".equals(contextoRegra.getPrePersistEntityState().getDao().getEntityName());
        DynamicVO cabVO = contextoRegra.getPrePersistEntityState().getNewVO();

        if (tgfCab) {
            DynamicVO tpoVO = tpoDAO.findOne("CODTIPOPER = ? AND DHALTER = ? AND CODMODDOC = 55 AND NFE = 'N' AND TIPMOV = 'V'", cabVO.asBigDecimalOrZero("CODTIPOPER"), cabVO.asTimestamp("DHTIPOPER"));

            boolean confirmado = JapeSession.getPropertyAsBoolean("CabecalhoNota.confirmando.nota", Boolean.FALSE);

            if (tpoVO !=  null && confirmado) {
                Collection<DynamicVO> itens = iteDAO.find("NUNOTA = ?", cabVO.asBigDecimalOrZero("NUNOTA"));

                for (DynamicVO item : itens) {
                    DynamicVO proVO = proDAO.findOne("CODPROD = ?", item.asBigDecimalOrZero("CODPROD"));

                    if (item.asString("AD_DESCRPRODCLI") != null && item.asString("AD_DESCRPRODCLI") != proVO.asString("DESCRPRODNFE")) {

//                        proDAO.prepareToUpdateByPK(item.asBigDecimalOrZero("CODPROD"))
//                                .set("DESCRPRODNFE", item.asString("AD_DESCRPRODCLI"))
//                                .update();

                        AcessoBanco acessoBanco = new AcessoBanco();
                        acessoBanco.openSession();
                        acessoBanco.update("UPDATE TGFPRO SET DESCRPRODNFE = ? WHERE CODPROD = ?", item.asString("AD_DESCRPRODCLI"), item.asBigDecimalOrZero("CODPROD"));
                        acessoBanco.closeSession();

                    } else if (item.asString("AD_DESCRPRODCLI") == null && proVO.asString("DESCRPROD") != proVO.asString("DESCRPRODNFE")) {

                        proDAO.prepareToUpdateByPK(item.asBigDecimalOrZero("CODPROD"))
                                .set("DESCRPRODNFE", proVO.asString("DESCRPROD"))
                                .update();
                    }
                }
            }
        }
    }

    @Override
    public void beforeDelete(ContextoRegra contextoRegra) throws Exception {

    }

    @Override
    public void afterInsert(ContextoRegra contextoRegra) throws Exception {

    }

    @Override
    public void afterUpdate(ContextoRegra contextoRegra) throws Exception {

    }

    @Override
    public void afterDelete(ContextoRegra contextoRegra) throws Exception {

    }
}

