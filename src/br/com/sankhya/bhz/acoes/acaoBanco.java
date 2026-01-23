package br.com.sankhya.bhz.acoes;

import br.com.sankhya.extensions.actionbutton.AcaoRotinaJava;
import br.com.sankhya.extensions.actionbutton.ContextoAcao;
import br.com.sankhya.jape.dao.JdbcWrapper;
import br.com.sankhya.jape.sql.NativeSql;
import br.com.sankhya.modelcore.util.EntityFacadeFactory;

public class acaoBanco implements AcaoRotinaJava {
    @Override
    public void doAction(ContextoAcao contexto) throws Exception {
        JdbcWrapper jdbc = EntityFacadeFactory.getDWFFacade().getJdbcWrapper();
        jdbc.openSession();
        NativeSql sql = new NativeSql(jdbc);

        sql.executeUpdate("CREATE OR REPLACE PROCEDURE STP_CALCULA_PRECO_DINAMICO(\n" +
                "    p_IdSessao VARCHAR2, \n" +
                "    p_Result OUT VARCHAR2\n" +
                ")\n" +
                "AS\n" +
                "    PRECO                 FLOAT := 0;\n" +
                "    PRECOFINAL            FLOAT := 0;\n" +
                "    V_VLRICMS             FLOAT := 0;\n" +
                "    V_ALIQUOTA            FLOAT := 0;\n" +
                "    V_ALIQUOTAESTRANGEIRA FLOAT := 0;\n" +
                "    V_NUNOTA              NUMBER;\n" +
                "    V_CODPROD             NUMBER;\n" +
                "    V_UTILIZAPRECO        CHAR(1);\n" +
                "    V_CODPARC             NUMBER;\n" +
                "    V_CODEMP              NUMBER; \n" +
                "    V_CODUFORIGEM         NUMBER; \n" +
                "    V_CODUFDESTINO        NUMBER;\n" +
                "    V_IDALIQICMS          NUMBER;\n" +
                "    V_SEQUENCIA           NUMBER;\n" +
                "    V_CODTAB              NUMBER;\n" +
                "    V_CLASSIFICMS         CHAR(1);\n" +
                "    V_VLRACRESC           FLOAT := 1;\n" +
                "\tV_TIPSUBST\t\t\t  CHAR(1);\n" +
                "\tV_ORIGPROD            CHAR(1);\n" +
                "\n" +
                "BEGIN\n" +
                "\n" +
                "    SELECT\n" +
                "    -- NVL(ACT_DEC_FIELD(p_IdSessao, 0, 'VLRUNIT'),0)\n" +
                "    NVL(ACT_DEC_FIELD(p_IdSessao, 0, 'VLRICMS'),0)\n" +
                "    -- , NVL(ACT_DEC_FIELD(p_IdSessao, 0, 'VLRSUBST'),0)\n" +
                "    -- , NVL(ACT_DEC_FIELD(p_IdSessao, 0, 'VLRIPI'),0)\n" +
                "    , NVL(ACT_DEC_FIELD(p_IdSessao, 0, 'NUNOTA'),0)\n" +
                "    , NVL(ACT_INT_FIELD(p_IdSessao, 0, 'SEQUENCIA'),0)\n" +
                "    , NVL(ACT_DEC_FIELD(p_IdSessao, 0, 'CODPARC'),0)\n" +
                "    -- , NVL(ACT_DEC_FIELD(p_IdSessao, 0, 'IDALIQ'),0)\n" +
                "    , NVL(ACT_DEC_FIELD(p_IdSessao, 0, 'CODPROD'),0)\n" +
                "\n" +
                "    INTO\n" +
                "    V_VLRICMS, V_NUNOTA, V_SEQUENCIA, V_CODPARC, V_CODPROD\n" +
                "    --VLRUNIT, VLRICMS, VLRSUBST, VLRIPI, NUNOTA, IDALIQ, CODPROD, CODPROD, CODPARC,\n" +
                "    --VLRUNIT, ICMSPRO_VLRDIFALDEST, ICMSPRO_VLRDIFALREM, ICMSPRO_VLRFCP, IPIPRO_PERCIPI, QTDNEG\n" +
                "    -- VLRUNIT, QTDNEG\n" +
                "\n" +
                "    FROM DUAL;\n" +
                "\n" +
                "    SELECT \n" +
                "        NVL(TPP.CODTAB,0) \n" +
                "    INTO V_CODTAB\n" +
                "    FROM \n" +
                "        TGFTPP TPP\n" +
                "        INNER JOIN TGFPAR PAR ON TPP.CODTIPPARC = PAR.CODTIPPARC\n" +
                "    WHERE \n" +
                "        PAR.CODPARC = V_CODPARC;\n" +
                "    -- V_NUTAB := NVL(ACT_DEC_FIELD(p_IdSessao, 0, 'NUTAB'),0);\n" +
                "\n" +
                "    -- RAISE_APPLICATION_ERROR(-20001, 'V_NUNOTA -> ' || V_NUNOTA );\n" +
                "    -- PRECO := V_CODPARC;\n" +
                "    -- p_Result := '{\"PRECO\":\"' || PRECO || '\"}';\n" +
                "    -- RETURN;\n" +
                "\n" +
                "    -- V_NUNOTA    := NVL(ACT_INT_FIELD(p_IdSessao, 0, 'NUNOTA'),0);\n" +
                "    -- V_CODPROD   := NVL(ACT_DEC_FIELD(p_IdSessao, 0, 'CODPROD'),0);\n" +
                "    -- V_SEQUENCIA := NVL(ACT_DEC_FIELD(p_IdSessao, 0, 'SEQUENCIA'),0);\n" +
                "\n" +
                "\n" +
                "    BEGIN\n" +
                "        SELECT SNK_PRECO(V_CODTAB, V_CODPROD)\n" +
                "            INTO PRECO\n" +
                "            FROM DUAL;\n" +
                "    EXCEPTION\n" +
                "        WHEN NO_DATA_FOUND THEN\n" +
                "            PRECO := 0;\n" +
                "            p_Result := '{\"PRECO\":\"' || PRECO || '\"}';\n" +
                "            RETURN;\n" +
                "    END;\n" +
                "    -- PRECO := V_CODPROD;\n" +
                "    -- p_Result := '{\"PRECO\":\"' || PRECO || '\"}';\n" +
                "    -- RETURN;\n" +
                "\n" +
                "\n" +
                "    BEGIN\n" +
                "        SELECT AD_PRECODINAMICO\n" +
                "        INTO V_UTILIZAPRECO\n" +
                "        FROM TGFCAB CAB\n" +
                "        INNER JOIN TGFTOP TOP ON CAB.CODTIPOPER = TOP.CODTIPOPER AND CAB.DHTIPOPER = TOP.DHALTER\n" +
                "        WHERE CAB.NUNOTA = V_NUNOTA;\n" +
                "    EXCEPTION\n" +
                "        WHEN NO_DATA_FOUND THEN\n" +
                "            p_Result := '{\"PRECO\":\"' || PRECO || '\"}';\n" +
                "            RETURN;\n" +
                "    END;\n" +
                "\n" +
                "\n" +
                "\n" +
                "    IF V_UTILIZAPRECO = 'S' THEN\n" +
                "\n" +
                "        BEGIN \n" +
                "            SELECT UFS.CODUF AS CODUFORIGEM, UFS2.CODUF AS CODUFDESTINO, PAR.CLASSIFICMS, CASE WHEN UFS.CODUF != UFS2.CODUF AND PAR.CLASSIFICMS = 'C' THEN 1.1 ELSE 1 END ACRESC\n" +
                "            INTO V_CODUFORIGEM, V_CODUFDESTINO, V_CLASSIFICMS, V_VLRACRESC\n" +
                "            FROM TGFCAB CAB\n" +
                "            INNER JOIN TSIEMP EMP ON EMP.CODEMP = CAB.CODEMP\n" +
                "            INNER JOIN TSICID CID ON CID.CODCID = EMP.CODCID\n" +
                "            INNER JOIN TSIUFS UFS ON UFS.CODUF = CID.UF\n" +
                "            INNER JOIN TGFPAR PAR ON PAR.CODPARC = CAB.CODPARC\n" +
                "            INNER JOIN TSICID CID2 ON CID2.CODCID = PAR.CODCID\n" +
                "            INNER JOIN TSIUFS UFS2 ON UFS2.CODUF = CID2.UF\n" +
                "            WHERE CAB.NUNOTA = V_NUNOTA;\n" +
                "        EXCEPTION\n" +
                "            WHEN NO_DATA_FOUND THEN\n" +
                "            p_Result := '{\"PRECO\":\"' || PRECO || '\"}';\n" +
                "            RETURN;\n" +
                "        END;\n" +
                "\n" +
                "\n" +
                "\n" +
                "        -- BUSCA DADOS FISCAIS DO PRODUTO\n" +
                "        BEGIN\n" +
                "    SELECT NVL(TIPSUBST, 'N'),\n" +
                "           NVL(ORIGPROD, '0')\n" +
                "    INTO V_TIPSUBST, V_ORIGPROD\n" +
                "    FROM TGFPRO\n" +
                "    WHERE CODPROD = V_CODPROD;\n" +
                "EXCEPTION\n" +
                "    WHEN NO_DATA_FOUND THEN\n" +
                "        p_Result := '{\"PRECO\":\"' || PRECO || '\"}';\n" +
                "        RETURN;\n" +
                "END;\n" +
                "\n" +
                "         \t   -- ORIGEM DO PRODUTO COM ALÍQUOTA FIXA 4%\n" +
                "        IF V_ORIGPROD IN ('1','2','3','8') \n" +
                "\t\t   AND V_CODUFORIGEM <> V_CODUFDESTINO THEN\n" +
                "\n" +
                "            PRECOFINAL := PRECO + (PRECO * (4 / 100));\n" +
                "            p_Result := '{\"PRECO\":\"' || PRECOFINAL || '\"}';\n" +
                "            RETURN;\n" +
                "        END IF;\n" +
                "\n" +
                "\n" +
                "\t\t   -- MESMO ESTADO OU CONSUMIDOR FINAL + PRODUTO ST\n" +
                "        IF ((V_CODUFORIGEM = V_CODUFDESTINO OR V_CLASSIFICMS = 'C')\n" +
                "            AND V_TIPSUBST <> 'N') THEN\n" +
                "            p_Result := '{\"PRECO\":\"' || PRECO || '\"}';\n" +
                "            RETURN;\n" +
                "        END IF;\n" +
                "\n" +
                "\n" +
                "\n" +
                "        IF V_CLASSIFICMS = 'C' THEN\n" +
                "            BEGIN \n" +
                "                SELECT ICM.ALIQUOTA\n" +
                "                INTO V_ALIQUOTA\n" +
                "                FROM TGFICM ICM\n" +
                "                WHERE ICM.UFORIG = V_CODUFORIGEM AND ICM.UFDEST = V_CODUFORIGEM\n" +
                "                AND TIPRESTRICAO = 'S'\n" +
                "                AND TIPRESTRICAO2 = 'S';\n" +
                "\n" +
                "            EXCEPTION\n" +
                "                WHEN NO_DATA_FOUND THEN\n" +
                "                p_Result := '{\"PRECO\":\"' || PRECO || '\"}';\n" +
                "                RETURN;\n" +
                "            END;\n" +
                "\n" +
                "            PRECOFINAL := PRECO + (PRECO * (V_ALIQUOTA / 100)); \n" +
                "            p_Result := '{\"PRECO\":\"' || PRECOFINAL || '\"}';\n" +
                "            RETURN;\n" +
                "        END IF;\n" +
                "\n" +
                "        -- ALIQUOTA EXTERNA\n" +
                "        BEGIN \n" +
                "            SELECT ICM.ALIQUOTA\n" +
                "            INTO V_ALIQUOTA\n" +
                "            FROM TGFICM ICM\n" +
                "            WHERE ICM.UFORIG = V_CODUFORIGEM AND ICM.UFDEST = V_CODUFDESTINO\n" +
                "            AND TIPRESTRICAO = 'S'\n" +
                "            AND TIPRESTRICAO2 = 'S';\n" +
                "\n" +
                "        EXCEPTION\n" +
                "            WHEN NO_DATA_FOUND THEN\n" +
                "            p_Result := '{\"PRECO\":\"' || PRECO || '\"}';\n" +
                "            RETURN;\n" +
                "        END;\n" +
                "\n" +
                "        PRECOFINAL := PRECO + (PRECO * (V_ALIQUOTA / 100)); \n" +
                "        p_Result := '{\"PRECO\":\"' || PRECOFINAL *V_VLRACRESC || '\"}';\n" +
                "        RETURN;\n" +
                "    ELSE\n" +
                "        PRECOFINAL := PRECO;\n" +
                "        p_Result := '{' ||\n" +
                "                    '\"PRECO\": \"' || PRECOFINAL || '\"' ||\n" +
                "                    '}';\n" +
                "    END IF;\n" +
                "\n" +
                "    RETURN;\n" +
                "\n" +
                "EXCEPTION\n" +
                "    WHEN OTHERS THEN\n" +
                "        p_Result := '{\"MSG_ERRO\": \"Erro: ' || REPLACE(SQLERRM, '\"', '''') || '\"}';\n" +
                "END;\n" +
                "\n" +
                "");

        jdbc.closeSession();

    }
}
