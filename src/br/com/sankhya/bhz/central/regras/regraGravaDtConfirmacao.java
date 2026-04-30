package br.com.sankhya.bhz.central.regras;

import br.com.sankhya.jape.core.JapeSession;
import br.com.sankhya.jape.vo.DynamicVO;
import br.com.sankhya.jape.wrapper.JapeFactory;
import br.com.sankhya.jape.wrapper.JapeWrapper;
import br.com.sankhya.modelcore.comercial.ContextoRegra;
import br.com.sankhya.modelcore.comercial.Regra;
import com.sankhya.util.TimeUtils;

public class regraGravaDtConfirmacao implements Regra {

    JapeWrapper cabDAO = JapeFactory.dao("CabecalhoNota");

    @Override
    public void beforeInsert(ContextoRegra ctx) throws Exception {

    }

    @Override
    public void beforeUpdate(ContextoRegra ctx) throws Exception {

    }

    @Override
    public void beforeDelete(ContextoRegra ctx) throws Exception {

    }

    @Override
    public void afterInsert(ContextoRegra ctx) throws Exception {

    }

    @Override
    public void afterUpdate(ContextoRegra ctx) throws Exception {

        DynamicVO cabVO = ctx.getPrePersistEntityState().getNewVO();

        boolean tgfCab = "CabecalhoNota".equals(ctx.getPrePersistEntityState().getDao().getEntityName());

        if (tgfCab) {
            boolean confirmando = JapeSession.getPropertyAsBoolean("CabecalhoNota.confirmando.nota", Boolean.FALSE);
            if(confirmando) {
                cabDAO.prepareToUpdateByPK(cabVO.asBigDecimalOrZero("NUNOTA"))
                        .set("AD_DTCONFIRMACAO", TimeUtils.getNow())
                        .update();
            }
        }

    }

    @Override
    public void afterDelete(ContextoRegra ctx) throws Exception {

    }
}
