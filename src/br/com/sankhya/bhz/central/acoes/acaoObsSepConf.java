package br.com.sankhya.bhz.central.acoes;

import br.com.sankhya.extensions.actionbutton.AcaoRotinaJava;
import br.com.sankhya.extensions.actionbutton.ContextoAcao;
import br.com.sankhya.extensions.actionbutton.Registro;
import br.com.sankhya.jape.vo.DynamicVO;
import br.com.sankhya.jape.wrapper.JapeFactory;
import br.com.sankhya.jape.wrapper.JapeWrapper;
import br.com.sankhya.modelcore.util.DynamicEntityNames;

import java.math.BigDecimal;

public class acaoObsSepConf implements AcaoRotinaJava {
    JapeWrapper cabDAO = JapeFactory.dao(DynamicEntityNames.CABECALHO_NOTA);

    @Override
    public void doAction(ContextoAcao contexto) throws Exception {
        Registro[] linhas = contexto.getLinhas();

        if (linhas.length <= 0) {
            contexto.mostraErro("Selecione pelo menos uma linha para atualização.");
        }

        BigDecimal nuNota = BigDecimal.ZERO;
        String obsSepConf = contexto.getParam("OBS").toString();

        for(Registro linha : linhas) {
            nuNota = (BigDecimal) linha.getCampo("NUNOTA");

            DynamicVO cabVO = cabDAO.findByPK(nuNota);

            cabDAO.prepareToUpdate(cabVO)
                    .set("AD_OBSSEPCONF", obsSepConf)
                    .update();
        }

        contexto.setMensagemRetorno("Observação Separador/Conferente informado com sucesso.");
    }
}
