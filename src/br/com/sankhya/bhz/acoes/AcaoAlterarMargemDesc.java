package br.com.sankhya.bhz.acoes;

import br.com.sankhya.bhz.utils.ErroUtils;
import br.com.sankhya.extensions.actionbutton.AcaoRotinaJava;
import br.com.sankhya.extensions.actionbutton.ContextoAcao;
import br.com.sankhya.extensions.actionbutton.Registro;
import br.com.sankhya.jape.vo.DynamicVO;
import br.com.sankhya.jape.wrapper.JapeFactory;
import br.com.sankhya.jape.wrapper.JapeWrapper;

import java.math.BigDecimal;
import java.util.Collection;

public class AcaoAlterarMargemDesc implements AcaoRotinaJava {
    @Override
    public void doAction(ContextoAcao contexto) throws Exception {
        JapeWrapper marDAO = JapeFactory.dao("AD_TGFMAREMP");
        JapeWrapper proDAO = JapeFactory.dao("Produto");
        JapeWrapper empDAO = JapeFactory.dao("EmpresaFinanceiro");
        BigDecimal margem = BigDecimal.ZERO;
        BigDecimal desc1 = BigDecimal.ZERO;
        BigDecimal desc2 = BigDecimal.ZERO;

        if(null!=contexto.getParam("MARGEM")){
            margem = new BigDecimal(contexto.getParam("MARGEM").toString());
        }

        if(null!=contexto.getParam("DESC1")){
            desc1 = new BigDecimal(contexto.getParam("DESC1").toString());
        }

        if(null!=contexto.getParam("DESC2")){
            desc2 = new BigDecimal(contexto.getParam("DESC2").toString());
        }


        Collection<DynamicVO> emp = empDAO.find("ATIVO = 'S'");
        for(DynamicVO empVO : emp) {
            Collection<DynamicVO> pro = proDAO.find("USOPROD = 'R' AND " +
                    "CODPROD NOT IN (SELECT CODPROD FROM AD_TGFMAREMP WHERE CODEMP = ?)",empVO.asBigDecimal("CODEMP"));
            for (DynamicVO proVO : pro) {
                marDAO.create()
                        .set("CODPROD",proVO.asBigDecimal("CODPROD"))
                        .set("CODEMP",empVO.asBigDecimal("CODEMP"))
                        .set("MARGEM", margem)
                        .set("DESC1", desc1)
                        .set("DESC2", desc2)
                        .save();
            }
        }
        if(contexto.getLinhas().length!=0) {
            Registro[] linhas = contexto.getLinhas();

            for (Registro linha : linhas) {

                DynamicVO marVO = marDAO.findOne("CODPROD = ? AND CODEMP = ?"
                        , linha.getCampo("CODPROD")
                        , linha.getCampo("CODEMP"));

                if (null != marVO) {
                    if (margem.compareTo(BigDecimal.ZERO) != 0) {
                        marDAO.prepareToUpdate(marVO)
                                .set("MARGEM", margem)
                                .update();
                    }
                    if (desc1.compareTo(BigDecimal.ZERO) != 0) {
                        marDAO.prepareToUpdate(marVO)
                                .set("DESC1", desc1)
                                .update();
                    }
                    if (desc2.compareTo(BigDecimal.ZERO) != 0) {
                        marDAO.prepareToUpdate(marVO)
                                .set("DESC2", desc2)
                                .update();
                    }
                }
            }
        }
    }
}
