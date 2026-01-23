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
import java.sql.Timestamp;

public class eventoValLib1001PosConf implements EventoProgramavelJava {
    JapeWrapper libDAO = JapeFactory.dao(DynamicEntityNames.LIBERACAO_LIMITE);
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

    }

    @Override
    public void afterUpdate(PersistenceEvent event) throws Exception {
        DynamicVO vo = (DynamicVO) event.getVo();

        BigDecimal nuChave = vo.asBigDecimalOrZero("NUCHAVE");
        BigDecimal evento = vo.asBigDecimalOrZero("EVENTO");
        BigDecimal vlrLib = vo.asBigDecimalOrZero("VLRLIBERADO");
        BigDecimal usuLib = vo.asBigDecimalOrZero("CODUSULIB");
        Timestamp dhLib = vo.asTimestamp("DHLIB");
        boolean aprovado = vo.asString("REPROVADO").equals("N");

        if (null != dhLib  && evento.equals(BigDecimal.valueOf(1001)) && vlrLib.compareTo(BigDecimal.ZERO) > 0 && aprovado) {
            DynamicVO libVO = libDAO.findOne("NUCHAVE = ? AND EVENTO = 3 AND REPROVADO = 'N' AND DHLIB IS NOT NULL AND VLRLIBERADO > 0", nuChave);

            if (null != libVO) {

                String obsLibNew =  "<br>Liberação alterada à partir da liberação do evento 1001 - Liberação Crédito após Confirmação. Histórico de Liberação Anterior: <br> "+
                        "Código Usuário Lib. Ant.: " + libVO.asBigDecimalOrZero("CODUSULIB")+", "+
                        "Valor Solicitado Ant.: " + libVO.asBigDecimalOrZero("VLRATUAL")+", "+
                        "Valor Liberado Ant.: " + libVO.asBigDecimalOrZero("VLRLIBERADO");

                libDAO.prepareToUpdate(libVO)
                        .set("DHLIB", dhLib)
                        .set("VLRLIBERADO", vlrLib)
                        .set("CODUSULIB", usuLib)
                        .set("OBSLIB", libVO.asString("OBSLIB") + obsLibNew)
                        .update();
            } else {
                ErroUtils.disparaErro("Não é possível realizar a liberação de alteração de crédito após a confirmação da nota, pois não foi identificada liberação de limite de crédito para o movimento.");
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
