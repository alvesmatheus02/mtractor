package br.com.sankhya.bhz.central.eventos;

import br.com.sankhya.bhz.utils.ErroUtils;
import br.com.sankhya.extensions.eventoprogramavel.EventoProgramavelJava;
import br.com.sankhya.jape.event.PersistenceEvent;
import br.com.sankhya.jape.event.TransactionContext;
import br.com.sankhya.jape.vo.DynamicVO;
import br.com.sankhya.jape.wrapper.JapeFactory;
import br.com.sankhya.jape.wrapper.JapeWrapper;
import br.com.sankhya.modelcore.util.DynamicEntityNames;

import java.math.BigDecimal;

public class eventoValLIbCredConferencia implements EventoProgramavelJava {
    JapeWrapper libDAO = JapeFactory.dao(DynamicEntityNames.LIBERACAO_LIMITE);
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

        BigDecimal nuNota = vo.asBigDecimalOrZero("NUNOTAORIG");
        DynamicVO vallibCred = libDAO.findOne("NUCHAVE = ? AND TABELA = 'TGFCAB' AND EVENTO = 3", nuNota);
        DynamicVO cabVO = cabDAO.findOne("NUNOTA = ? AND NULLVALUE(AD_PEDALTPOSCONF,'N') IN ('A', 'P', 'R')", nuNota);

        if (null != cabVO) {
            if (null != vallibCred) {
                if (cabVO.asString("AD_PEDALTPOSCONF").equals("A")) {
                    ErroUtils.disparaErro("O pedido foi alterado e é necessário revisão da liberação de crédito do mesmo. Favor comunicar o setor responsável e aguardar análise.");
                } else if (vallibCred.asString("REPROVADO").equals("S")) {
                    ErroUtils.disparaErro("Liberação de limite de crédito recusada. Favor consultar a liberação existente e revisar a solicitação.");
                } else if (vallibCred.asString("REPROVADO").equals("N") && null == vallibCred.asTimestamp("DHLIB") && vallibCred.asBigDecimalOrZero("VLRLIBERADO").compareTo(BigDecimal.ZERO) == 0) {
                    ErroUtils.disparaErro("O pedido já possui solicitação de liberação de limite de crédito em andamento. Favor aguardar a análise.");
                }
            } else {
                ErroUtils.disparaErro("O pedido foi alterado após a confirmação. Sua liberação deverá ser revisada. Favor comunicar o setor responsável e aguardar análise.");
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
