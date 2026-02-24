package br.com.sankhya.bhz.utils;

import java.math.BigDecimal;

public class stringToBigdecimal {
    public static BigDecimal verificaCelula(String celula) throws Exception {
        BigDecimal result;
        //if(celula.isEmpty() || "null".equals(celula)){
        if(null == celula){
            //if("null".equals(celula)){
            result = BigDecimal.ZERO;
        }else {
            result = new BigDecimal(celula.replace(",","."));
        }
        return result;
    }
}
