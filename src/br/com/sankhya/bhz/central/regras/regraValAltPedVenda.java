package br.com.sankhya.bhz.central.regras;

import br.com.sankhya.bhz.utils.AcessoBanco;
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
    JapeWrapper cabDAO = JapeFactory.dao(DynamicEntityNames.CABECALHO_NOTA);
    @Override
    public void beforeInsert(ContextoRegra ctx) throws Exception {

    }

    @Override
    public void beforeUpdate(ContextoRegra ctx) throws Exception {
        DynamicVO vo = ctx.getPrePersistEntityState().getNewVO();

        BigDecimal nuNota = vo.asBigDecimalOrZero("NUNOTA");

        DynamicVO cabVO = cabDAO.findByPK(nuNota);
        DynamicVO cabConfVO = cabConfDAO.findOne("NUNOTAORIG = ?", nuNota);

        boolean tgfIte = "ItemNota".equals(ctx.getPrePersistEntityState().getDao().getEntityName());
        boolean tgfCab = "CabecalhoNota".equals(ctx.getPrePersistEntityState().getDao().getEntityName());
        boolean pedido = cabVO.asString("TIPMOV").equals("P");
        boolean confirmado = cabVO.asString("STATUSNOTA").equals("L");

        if (null != cabConfVO && pedido && confirmado) {
            ErroUtils.disparaErro("Alterações no pedido não são permitidas após o início do processo de conferência. Solicito, por gentileza, alinhar com o setor responsável.");
        } else if (null == cabConfVO && pedido && confirmado) {
            AcessoBanco acessoBanco = new AcessoBanco();
            acessoBanco.openSession();
            acessoBanco.update("UPDATE TGFCAB SET AD_PEDALTPOSCONF = 'A' WHERE NUNOTA = ?", nuNota);
            acessoBanco.closeSession();
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
