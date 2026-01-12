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

public class acaoControlAssinaturaNF implements AcaoRotinaJava {
    JapeWrapper cabDAO = JapeFactory.dao(DynamicEntityNames.CABECALHO_NOTA);
    @Override
    public void doAction(ContextoAcao contexto) throws Exception {
        Registro[] linhas = contexto.getLinhas();

        if (linhas.length <= 0) {
            contexto.mostraErro("Selecione pelo menos uma linha para atualização.");
        }

        BigDecimal nuNota = BigDecimal.ZERO;
        String statusAss = (String) contexto.getParam("STATUSASS");

        for(Registro linha : linhas) {
            nuNota = (BigDecimal) linha.getCampo("NUNOTA");

            DynamicVO cabVO = cabDAO.findByPK(nuNota);

            if (cabVO.asString("TIPMOV").equals("V")) {
                linha.setCampo("AD_STATUSASSINATURA", statusAss);
            }
        }

        contexto.setMensagemRetorno("Assinaturas atualizadas com sucesso!");
    }
}
