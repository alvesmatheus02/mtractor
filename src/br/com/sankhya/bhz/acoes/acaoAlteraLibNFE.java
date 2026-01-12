package br.com.sankhya.bhz.acoes;

import br.com.sankhya.extensions.actionbutton.AcaoRotinaJava;
import br.com.sankhya.extensions.actionbutton.ContextoAcao;
import br.com.sankhya.extensions.actionbutton.Registro;
import br.com.sankhya.jape.vo.DynamicVO;
import br.com.sankhya.jape.wrapper.JapeFactory;
import br.com.sankhya.jape.wrapper.JapeWrapper;
import br.com.sankhya.modelcore.util.DynamicEntityNames;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

public class acaoAlteraLibNFE implements AcaoRotinaJava {
    JapeWrapper cabDAO = JapeFactory.dao(DynamicEntityNames.CABECALHO_NOTA);
    JapeWrapper conDAO = JapeFactory.dao("CabecalhoConferencia");
    @Override
    public void doAction(ContextoAcao contexto) throws Exception {
        Registro[] linhas = contexto.getLinhas();

        if (linhas.length <= 0) {
            contexto.mostraErro("Selecione pelo menos uma linha para atualização.");
        }

        BigDecimal nuNota = BigDecimal.ZERO;
        BigDecimal countNaoUPD = BigDecimal.ZERO;
        String statusPed = (String) contexto.getParam("STATUSPED");
        List<String> nuNotaNaoUPD = new ArrayList<>();

        if (statusPed.equals("V")) {
            statusPed = null;
        }

        for(Registro linha : linhas) {
            nuNota = (BigDecimal) linha.getCampo("NUNOTA");

            DynamicVO cabVO = cabDAO.findByPK(nuNota);

            if (cabVO.asBigDecimalOrZero("NUCONFATUAL").compareTo(BigDecimal.ZERO) > 0) {
                DynamicVO conVO = conDAO.findByPK(cabVO.asBigDecimalOrZero("NUCONFATUAL"));

                if (null != conVO && null != conVO.asString("STATUS") && (conVO.asString("STATUS").equals("D") || conVO.asString("STATUS").equals("F"))) {
                    linha.setCampo("AD_STATUSPED", statusPed);
                } else {
                    countNaoUPD = countNaoUPD.add(BigDecimal.ONE);
                    nuNotaNaoUPD.add(nuNota.toString());
                }
            } else {
                DynamicVO conVO = conDAO.findOne("NUNOTAORIG = ?", nuNota);

                if (null != conVO && null != conVO.asString("STATUS") && (conVO.asString("STATUS").equals("D") || conVO.asString("STATUS").equals("F"))) {
                    linha.setCampo("AD_STATUSPED", statusPed);
                } else {
                    countNaoUPD = countNaoUPD.add(BigDecimal.ONE);
                    nuNotaNaoUPD.add(nuNota.toString());
                }
            }
        }

        if (countNaoUPD.compareTo(BigDecimal.ZERO) > 0) {
            contexto.setMensagemRetorno("Pedidos atualizados com excessão dos números únicos :"+nuNotaNaoUPD);
        } else {
            contexto.setMensagemRetorno("Pedidos atualizados com sucesso!"+countNaoUPD);
        }
    }
}
