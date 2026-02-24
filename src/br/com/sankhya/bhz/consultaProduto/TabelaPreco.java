package br.com.sankhya.bhz.consultaProduto;

import br.com.sankhya.bhz.utils.ErroUtils;
import br.com.sankhya.jape.EntityFacade;
import br.com.sankhya.jape.dao.JdbcWrapper;
import br.com.sankhya.jape.sql.NativeSql;
import br.com.sankhya.modelcore.custompanel.CustomPanel;
import br.com.sankhya.modelcore.custompanel.CustomPanelResult;
import br.com.sankhya.modelcore.util.EntityFacadeFactory;

import java.math.BigDecimal;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.*;

public class TabelaPreco implements CustomPanel {
    @Override
    public CustomPanelResult buildResult(Map<String, Object> parameters) throws SQLException {

        BigDecimal nuNota = BigDecimal.ZERO;
        BigDecimal codProd = BigDecimal.ZERO;
        BigDecimal codParc = BigDecimal.ONE;
        CustomPanelResult customPanelResult = new CustomPanelResult();
        Collection<Map<String, Object>> linhas = new ArrayList<>();
        try {
            if (parameters.containsKey("NUNOTA") && parameters.get("NUNOTA") != null) {
                nuNota = (BigDecimal) parameters.get("NUNOTA");
            }

            if (parameters.containsKey("CODPROD") && parameters.get("CODPROD") != null) {
                codProd = (BigDecimal) parameters.get("CODPROD");
            }

            if (parameters.containsKey("CODPARC") && parameters.get("CODPARC") != null) {
                codParc = (BigDecimal) parameters.get("CODPARC");
            }

            EntityFacade dwfFacade = EntityFacadeFactory.getDWFFacade();
            JdbcWrapper jdbc = dwfFacade.getJdbcWrapper();
            jdbc.openSession();
            NativeSql sql = new NativeSql(jdbc);
            sql.loadSql(TabelaPreco.class, "BuscaPreco.sql");
            sql.setNamedParameter("NUNOTA",nuNota);
            sql.setNamedParameter("CODPROD",codProd);
            sql.setNamedParameter("CODPARC",codParc);
            ResultSet resultSet = sql.executeQuery();
            while (resultSet.next()){
                Map<String, Object> campos = new HashMap<>();
                campos.put("CODEMP",resultSet.getBigDecimal("EMPRESA"));
                campos.put("DESCRICAO",resultSet.getBigDecimal("DESCRICAO"));
                campos.put("PRECO",resultSet.getBigDecimal("PRECO"));
                campos.put("CODPROD",codProd);
                campos.put("CODVOL","UN");
                campos.put("CODLOCAL",BigDecimal.ZERO);
                campos.put("CONTROLE"," ");
                linhas.add(campos);
            }

        } catch (Exception e) {
            throw new RuntimeException(e);
        }

        customPanelResult.setResult(linhas);

        return customPanelResult;
    }

    @Override
    public CustomPanelResult getMetadados(Map<String, Object> parameters) {
        return null;
    }

    @Override
    public Class<?> getInterfaceToSearch() {
        return null;
    }
}
