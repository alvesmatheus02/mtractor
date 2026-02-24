package br.com.sankhya.bhz.eventos;

import br.com.sankhya.extensions.eventoprogramavel.EventoProgramavelJava;
import br.com.sankhya.jape.event.PersistenceEvent;
import br.com.sankhya.jape.event.TransactionContext;
import br.com.sankhya.jape.vo.DynamicVO;
import br.com.sankhya.jape.wrapper.JapeFactory;
import br.com.sankhya.jape.wrapper.JapeWrapper;

import java.util.Collection;

public class CompletaCadastroProduto implements EventoProgramavelJava {
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
        DynamicVO proVO = (DynamicVO) event.getVo();
        JapeWrapper empMarDAO = JapeFactory.dao("AD_TGFMAREMP");
        JapeWrapper empDAO = JapeFactory.dao("EmpresaFinanceiro");
        Collection<DynamicVO> emp = empDAO.find("ATIVO = 'S'");

        for(DynamicVO empVO : emp) {
            DynamicVO empMarVO = empMarDAO.findOne("CODPROD = ? AND CODEMP = ?",proVO.asBigDecimal("CODPROD"), empVO.asBigDecimal("CODEMP"));
            if(null==empMarVO) {
                empMarDAO.create()
                        .set("CODEMP", empVO.asBigDecimal("CODEMP"))
                        .set("CODPROD", proVO.asBigDecimal("CODPROD"))
                        .save();
            }
        }
    }

    @Override
    public void afterUpdate(PersistenceEvent event) throws Exception {

    }

    @Override
    public void afterDelete(PersistenceEvent event) throws Exception {

    }

    @Override
    public void beforeCommit(TransactionContext tranCtx) throws Exception {

    }
}
