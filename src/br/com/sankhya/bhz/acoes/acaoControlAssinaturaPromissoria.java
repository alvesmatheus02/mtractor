package br.com.sankhya.bhz.acoes;

import br.com.sankhya.bhz.utils.AcessoBanco;
import br.com.sankhya.extensions.actionbutton.AcaoRotinaJava;
import br.com.sankhya.extensions.actionbutton.ContextoAcao;
import br.com.sankhya.extensions.actionbutton.Registro;
import br.com.sankhya.jape.vo.DynamicVO;
import br.com.sankhya.jape.wrapper.JapeFactory;
import br.com.sankhya.jape.wrapper.JapeWrapper;
import br.com.sankhya.modelcore.util.DynamicEntityNames;

import java.math.BigDecimal;

public class acaoControlAssinaturaPromissoria implements AcaoRotinaJava {
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
                AcessoBanco acessoBanco = new AcessoBanco();
                acessoBanco.openSession();
                acessoBanco.update("UPDATE TGFCAB SET AD_STATUSASSINATURAPROM = '"+statusAss+"' WHERE NUNOTA = "+nuNota);
                acessoBanco.closeSession();
            }
        }

        contexto.setMensagemRetorno("Assinaturas Promissórias atualizadas com sucesso!");
    }
}
