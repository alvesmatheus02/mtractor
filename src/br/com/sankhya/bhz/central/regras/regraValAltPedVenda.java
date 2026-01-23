package br.com.sankhya.bhz.central.regras;

import br.com.sankhya.bhz.utils.ErroUtils;
import br.com.sankhya.jape.vo.DynamicVO;
import br.com.sankhya.jape.wrapper.JapeFactory;
import br.com.sankhya.jape.wrapper.JapeWrapper;
import br.com.sankhya.modelcore.comercial.ContextoRegra;
import br.com.sankhya.modelcore.comercial.Regra;
import br.com.sankhya.modelcore.util.DynamicEntityNames;

import java.math.BigDecimal;

public class regraValAltPedVenda implements Regra {

    JapeWrapper cabConfDAO = JapeFactory.dao("CabecalhoConferencia");


    @Override
    public void beforeInsert(ContextoRegra ctx) throws Exception {

    }

    @Override
    public void beforeUpdate(ContextoRegra ctx) throws Exception {
        DynamicVO vo = ctx.getPrePersistEntityState().getNewVO();

        boolean tgfIte = "ItemNota".equals(ctx.getPrePersistEntityState().getDao().getEntityName());
        boolean tgfCab = "CabecalhoNota".equals(ctx.getPrePersistEntityState().getDao().getEntityName());
        BigDecimal nuNota = vo.asBigDecimalOrZero("NUNOTA");

        DynamicVO cabConfVO = cabConfDAO.findOne("NUNOTAORIG = ?", nuNota);

        if (null != cabConfVO) {
            ErroUtils.disparaErro("Alterações no pedido não são permitidas após o início do processo de conferência. Solicito, por gentileza, alinhar com o setor responsável.");
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
