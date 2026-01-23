package br.com.sankhya.bhz.central.eventos;

import br.com.sankhya.bhz.utils.ErroUtils;
import br.com.sankhya.extensions.eventoprogramavel.EventoProgramavelJava;
import br.com.sankhya.jape.event.PersistenceEvent;
import br.com.sankhya.jape.event.TransactionContext;
import br.com.sankhya.jape.vo.DynamicVO;
import br.com.sankhya.jape.wrapper.JapeFactory;
import br.com.sankhya.jape.wrapper.JapeWrapper;
import br.com.sankhya.modelcore.auth.AuthenticationInfo;
import br.com.sankhya.modelcore.util.DynamicEntityNames;
import com.sankhya.util.TimeUtils;

import java.math.BigDecimal;

public class eventoValLimCredPedConfirmado implements EventoProgramavelJava {
    JapeWrapper libDAO = JapeFactory.dao(DynamicEntityNames.LIBERACAO_LIMITE);
    @Override
    public void beforeInsert(PersistenceEvent event) throws Exception {

    }

    @Override
    public void beforeUpdate(PersistenceEvent event) throws Exception {
        DynamicVO cabVO = (DynamicVO) event.getVo();

        boolean altVlrTot = event.getModifingFields().isModifing("VLRNOTA");
        boolean confirmada = cabVO.asString("STATUSNOTA").equals("L");
        boolean pedVenda = cabVO.asString("TIPMOV").equals("P");
        BigDecimal vlrNota = cabVO.asBigDecimalOrZero("VLRNOTA");
        BigDecimal nuNota = cabVO.asBigDecimalOrZero("NUNOTA");

        if (altVlrTot && confirmada && pedVenda) {
            DynamicVO libVO = libDAO.findOne("NUCHAVE = ? AND EVENTO = 3 AND REPROVADO = 'N' AND DHLIB IS NOT NULL AND VLRLIBERADO > 0", nuNota);

            if (null != libVO && vlrNota.compareTo(libVO.asBigDecimalOrZero("VLRLIBERADO")) > 0) {

                libDAO.create()
                        .set("NUCHAVE", nuNota)
                        .set("SEQUENCIA", BigDecimal.ONE)
                        .set("TABELA", "TGFCAB")
                        .set("EVENTO", BigDecimal.valueOf(1001))
                        .set("CODUSUSOLICIT", AuthenticationInfo.getCurrent().getUserID())
                        .set("DHSOLICIT", TimeUtils.getNow())
                        .set("VLRATUAL", cabVO.asBigDecimalOrZero("VLRNOTA"))
                        .set("CODCENCUS", cabVO.asBigDecimalOrZero("CODCENCUS"))
                        .set("CODUSULIB", BigDecimal.ZERO)
                        .set("OBSERVACAO", "Alteração pedido após confirmação do pedido, valor limite liberado de R$"+libVO.asBigDecimalOrZero("VLRLIBERADO")+", novo valor solicitado de R$"+vlrNota+".")
                        .set("VLRLIMITE", BigDecimal.ZERO)
                        .save();
            }
        }

    }

    @Override
    public void beforeDelete(PersistenceEvent event) throws Exception {

    }

    @Override
    public void afterInsert(PersistenceEvent event) throws Exception {

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
