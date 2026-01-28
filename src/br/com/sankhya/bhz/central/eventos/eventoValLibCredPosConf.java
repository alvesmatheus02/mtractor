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

public class eventoValLibCredPosConf implements EventoProgramavelJava {
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

    }

    @Override
    public void afterUpdate(PersistenceEvent event) throws Exception {
        DynamicVO vo = (DynamicVO) event.getVo();

        BigDecimal nuChave = vo.asBigDecimalOrZero("NUCHAVE");
        BigDecimal evento = vo.asBigDecimalOrZero("EVENTO");
        BigDecimal vlrLib = vo.asBigDecimalOrZero("VLRLIBERADO");
        Timestamp dhLib = vo.asTimestamp("DHLIB");
        String tabela = vo.asString("TABELA");
        String reprovado = vo.asString("REPROVADO");

        if (evento.equals(BigDecimal.valueOf(3)) && tabela.equals("TGFCAB")) {
            DynamicVO cabVO = cabDAO.findOne("NUNOTA = ? AND NULLVALUE(AD_PEDALTPOSCONF,'N') != 'N'", nuChave);

            if (null != cabVO) {
                if (reprovado.equals("S")) {
                    cabDAO.prepareToUpdate(cabVO)
                            .set("AD_PEDALTPOSCONF", "R")
                            .update();
                } else if (reprovado.equals("N") && null != dhLib && vlrLib.compareTo(BigDecimal.ZERO) > 0) {
                    cabDAO.prepareToUpdate(cabVO)
                            .set("AD_PEDALTPOSCONF", "L")
                            .update();
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
