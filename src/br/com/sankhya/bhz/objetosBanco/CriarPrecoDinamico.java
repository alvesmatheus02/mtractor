package br.com.sankhya.bhz.objetosBanco;

import br.com.sankhya.extensions.actionbutton.AcaoRotinaJava;
import br.com.sankhya.extensions.actionbutton.ContextoAcao;
import br.com.sankhya.jape.EntityFacade;
import br.com.sankhya.jape.dao.JdbcWrapper;
import br.com.sankhya.jape.sql.NativeSql;
import br.com.sankhya.modelcore.util.EntityFacadeFactory;

public class CriarPrecoDinamico implements AcaoRotinaJava {
    @Override
    public void doAction(ContextoAcao contexto) throws Exception {
        EntityFacade dwfFacade = EntityFacadeFactory.getDWFFacade();
        JdbcWrapper jdbc = dwfFacade.getJdbcWrapper();
        jdbc.openSession();
        NativeSql sql = new NativeSql(jdbc);
        sql.loadSql(CriarPrecoDinamico.class, "STP_CALCULA_PRECO_DINAMICO.sql");
        sql.executeUpdate();
    }
}
