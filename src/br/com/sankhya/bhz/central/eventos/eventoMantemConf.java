package br.com.sankhya.bhz.central.eventos;

import br.com.sankhya.extensions.eventoprogramavel.EventoProgramavelJava;
import br.com.sankhya.jape.event.PersistenceEvent;
import br.com.sankhya.jape.event.TransactionContext;
import br.com.sankhya.jape.vo.DynamicVO;
import br.com.sankhya.jape.wrapper.JapeFactory;
import br.com.sankhya.jape.wrapper.JapeWrapper;
import br.com.sankhya.modelcore.util.DynamicEntityNames;

import java.math.BigDecimal;

public class eventoMantemConf implements EventoProgramavelJava {
    JapeWrapper conDAO = JapeFactory.dao("CabecalhoConferencia");
    JapeWrapper cabDAO = JapeFactory.dao(DynamicEntityNames.CABECALHO_NOTA);


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
        DynamicVO conAntVO = conDAO.findOne("NUNOTAORIG = ? AND AD_CODCONF IS NOT NULL", vo.asBigDecimalOrZero("NUNOTAORIG"));

        if (vo.asBigDecimalOrZero("AD_CODCONF").equals(BigDecimal.ZERO) && null != conAntVO && !conAntVO.asBigDecimalOrZero("AD_CODCONF").equals(BigDecimal.ZERO)) {
            conDAO.prepareToUpdateByPK(vo.asBigDecimalOrZero("NUCONF"))
                    .set("AD_CODCONF", conAntVO.asBigDecimalOrZero("AD_CODCONF"))
                    .set("AD_CODCONFSEP", conAntVO.asBigDecimalOrZero("AD_CODCONFSEP"))
                    .update();

            cabDAO.prepareToUpdateByPK(vo.asBigDecimalOrZero("NUNOTAORIG"))
                    .set("AD_CODCONF", conAntVO.asBigDecimalOrZero("AD_CODCONF"))
                    .set("AD_CODCONFSEP", conAntVO.asBigDecimalOrZero("AD_CODCONFSEP"))
                    .update();
        } else if (!vo.asBigDecimalOrZero("AD_CODCONF").equals(BigDecimal.ZERO)) {
            cabDAO.prepareToUpdateByPK(vo.asBigDecimalOrZero("NUNOTAORIG"))
                    .set("AD_CODCONF", vo.asBigDecimalOrZero("AD_CODCONF"))
                    .set("AD_CODCONFSEP", vo.asBigDecimalOrZero("AD_CODCONFSEP"))
                    .update();
        }

    }

    @Override
    public void afterUpdate(PersistenceEvent event) throws Exception {
        DynamicVO vo = (DynamicVO) event.getVo();
        DynamicVO conAntVO = conDAO.findOne("NUNOTAORIG = ? AND AD_CODCONF IS NOT NULL", vo.asBigDecimalOrZero("NUNOTAORIG"));

        if (vo.asBigDecimalOrZero("AD_CODCONF").equals(BigDecimal.ZERO) && null != conAntVO && !conAntVO.asBigDecimalOrZero("AD_CODCONF").equals(BigDecimal.ZERO)) {
            conDAO.prepareToUpdateByPK(vo.asBigDecimalOrZero("NUCONF"))
                    .set("AD_CODCONF", conAntVO.asBigDecimalOrZero("AD_CODCONF"))
                    .set("AD_CODCONFSEP", conAntVO.asBigDecimalOrZero("AD_CODCONFSEP"))
                    .update();

            cabDAO.prepareToUpdateByPK(vo.asBigDecimalOrZero("NUNOTAORIG"))
                    .set("AD_CODCONF", conAntVO.asBigDecimalOrZero("AD_CODCONF"))
                    .set("AD_CODCONFSEP", conAntVO.asBigDecimalOrZero("AD_CODCONFSEP"))
                    .update();
        } else if (!vo.asBigDecimalOrZero("AD_CODCONF").equals(BigDecimal.ZERO)) {
            cabDAO.prepareToUpdateByPK(vo.asBigDecimalOrZero("NUNOTAORIG"))
                    .set("AD_CODCONF", vo.asBigDecimalOrZero("AD_CODCONF"))
                    .set("AD_CODCONFSEP", vo.asBigDecimalOrZero("AD_CODCONFSEP"))
                    .update();
        }
    }

    @Override
    public void afterDelete(PersistenceEvent event) throws Exception {

    }

    @Override
    public void beforeCommit(TransactionContext tranCtx) throws Exception {

    }
}
