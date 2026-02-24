package br.com.sankhya.bhz.eventos;

import br.com.sankhya.bhz.utils.AcessoBanco;
import br.com.sankhya.extensions.eventoprogramavel.EventoProgramavelJava;
import br.com.sankhya.jape.event.PersistenceEvent;
import br.com.sankhya.jape.event.TransactionContext;
import br.com.sankhya.jape.vo.DynamicVO;
import br.com.sankhya.jape.wrapper.JapeFactory;
import br.com.sankhya.jape.wrapper.JapeWrapper;
import br.com.sankhya.modelcore.util.DynamicEntityNames;

import java.math.BigDecimal;

public class eventoTipoNegPortaisFin implements EventoProgramavelJava {
    JapeWrapper cabDAO = JapeFactory.dao(DynamicEntityNames.CABECALHO_NOTA);
    JapeWrapper finDAO = JapeFactory.dao(DynamicEntityNames.FINANCEIRO);

    @Override
    public void beforeInsert(PersistenceEvent event) throws Exception {

    }

    @Override
    public void beforeUpdate(PersistenceEvent event) throws Exception {

    }

    @Override
    public void beforeDelete(PersistenceEvent event) throws Exception {

    }

    @Override
    public void afterInsert(PersistenceEvent event) throws Exception {
        DynamicVO vo = (DynamicVO) event.getVo();

        BigDecimal nuNota = vo.asBigDecimalOrZero("NUNOTA");
        String origem = vo.asString("ORIGEM");

        if (null != origem && origem.equals("E") && nuNota.compareTo(BigDecimal.ZERO) > 0) {
            DynamicVO cabVO = cabDAO.findByPK(nuNota);

            if (null != cabVO && cabVO.asBigDecimalOrZero("CODTIPVENDA").compareTo(BigDecimal.ZERO) > 0) {
                AcessoBanco acessoBanco = new AcessoBanco();
                acessoBanco.openSession();
                acessoBanco.update("UPDATE TGFFIN SET AD_CODTIPVENDA = ? WHERE NUFIN = ?", cabVO.asBigDecimalOrZero("CODTIPVENDA"), vo.asBigDecimalOrZero("NUFIN"));
                acessoBanco.closeSession();
//                finDAO.prepareToUpdate(vo)
//                        .set("AD_CODTIPVENDA", cabVO.asBigDecimalOrZero("CODTIPVENDA"))
//                        .update();
            }
        }

    }

    @Override
    public void afterUpdate(PersistenceEvent event) throws Exception {
        DynamicVO vo = (DynamicVO) event.getVo();

        BigDecimal nuNota = vo.asBigDecimalOrZero("NUNOTA");
        String origem = vo.asString("ORIGEM");

        if (null != origem && origem.equals("E") && nuNota.compareTo(BigDecimal.ZERO) > 0) {
            DynamicVO cabVO = cabDAO.findByPK(nuNota);

            if (null != cabVO && cabVO.asBigDecimalOrZero("CODTIPVENDA").compareTo(BigDecimal.ZERO) > 0) {
                if (!cabVO.asBigDecimalOrZero("CODTIPVENDA").equals(vo.asBigDecimalOrZero("AD_CODTIPVENDA"))) {

                    AcessoBanco acessoBanco = new AcessoBanco();
                    acessoBanco.openSession();
                    acessoBanco.update("UPDATE TGFFIN SET AD_CODTIPVENDA = ? WHERE NUFIN = ?", cabVO.asBigDecimalOrZero("CODTIPVENDA"), vo.asBigDecimalOrZero("NUFIN"));
                    acessoBanco.closeSession();
//                finDAO.prepareToUpdate(vo)
//                        .set("AD_CODTIPVENDA", cabVO.asBigDecimalOrZero("CODTIPVENDA"))
//                        .update();
                }
            }
        }
    }

    @Override
    public void afterDelete(PersistenceEvent event) throws Exception {

    }

    @Override
    public void beforeCommit(TransactionContext tranCtx) throws Exception {

    }
}
