package br.com.sankhya.bhz.central.acoes;

import br.com.sankhya.extensions.actionbutton.AcaoRotinaJava;
import br.com.sankhya.extensions.actionbutton.ContextoAcao;
import br.com.sankhya.extensions.actionbutton.Registro;
import br.com.sankhya.jape.vo.DynamicVO;
import br.com.sankhya.jape.wrapper.JapeFactory;
import br.com.sankhya.jape.wrapper.JapeWrapper;
import br.com.sankhya.modelcore.auth.AuthenticationInfo;
import br.com.sankhya.modelcore.util.DynamicEntityNames;
import com.sankhya.util.TimeUtils;
import org.apache.strutsel.taglib.html.ELSelectTag;

import javax.naming.AuthenticationException;
import java.math.BigDecimal;

public class acaoMarcaExpedido implements AcaoRotinaJava {
    JapeWrapper cabDAO = JapeFactory.dao(DynamicEntityNames.CABECALHO_NOTA);

    @Override
    public void doAction(ContextoAcao contexto) throws Exception {
        Registro[] linhas = contexto.getLinhas();

        if (linhas.length <= 0) {
            contexto.mostraErro("Selecione pelo menos uma linha para atualização.");
        }

        BigDecimal codUsu = new BigDecimal(contexto.getParam("CODUSU").toString());
        BigDecimal nuNota = BigDecimal.ZERO;
        String sucess = "S";

        for(Registro linha : linhas) {
            nuNota = (BigDecimal) linha.getCampo("NUNOTA");

            DynamicVO cabVO = cabDAO.findByPK(nuNota);

            if (null == cabVO.asString("AD_EXPEDIDO") || cabVO.asString("AD_EXPEDIDO").equals("N")) {
                cabDAO.prepareToUpdate(cabVO)
                        .set("AD_EXPEDIDO", "S")
                        .set("AD_DHEXPEDICAO", TimeUtils.getNow())
                        .set("AD_CODUSUEXPE", codUsu)
                        .update();
            } else {
                sucess = "N";
            }
        }

        if (sucess.equals("S")) {
            contexto.setMensagemRetorno("Pedido(s) selecionado(s) expedido(s) com sucesso.");
        } else {
            contexto.setMensagemRetorno("Pedido(s) selecionado(s) expedido(s) com sucesso. Porém, alguns já haviam sido expedidos. Revise a seleção.");
        }
    }
}
