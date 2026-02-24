CREATE OR REPLACE PROCEDURE STP_CALCULA_PRECO_DINAMICO(
    p_IdSessao VARCHAR2,
    p_Result OUT VARCHAR2
)
AS
    PRECO                 FLOAT := 0;
    PRECOFINAL            FLOAT := 0;
    V_VLRICMS             FLOAT := 0;
    V_ALIQUOTA            FLOAT := 0;
    V_ALIQUOTAESTRANGEIRA FLOAT := 0;
    V_NUNOTA              NUMBER;
    V_CODPROD             NUMBER;
    V_UTILIZAPRECO        CHAR(1);
    V_CODPARC             NUMBER;
    V_CODEMP              NUMBER;
    V_CODUFORIGEM         NUMBER;
    V_CODUFDESTINO        NUMBER;
    V_IDALIQICMS          NUMBER;
    V_SEQUENCIA           NUMBER;
    V_CODTAB              NUMBER;
    V_CLASSIFICMS         CHAR(1);

	V_TIPSUBST			  CHAR(1);
	V_ORIGPROD            CHAR(1);

BEGIN

    SELECT
    -- NVL(ACT_DEC_FIELD(p_IdSessao, 0, 'VLRUNIT'),0)
    NVL(ACT_DEC_FIELD(p_IdSessao, 0, 'VLRICMS'),0)
    -- , NVL(ACT_DEC_FIELD(p_IdSessao, 0, 'VLRSUBST'),0)
    -- , NVL(ACT_DEC_FIELD(p_IdSessao, 0, 'VLRIPI'),0)
    , NVL(ACT_DEC_FIELD(p_IdSessao, 0, 'NUNOTA'),0)
    , NVL(ACT_INT_FIELD(p_IdSessao, 0, 'SEQUENCIA'),0)
    , NVL(ACT_DEC_FIELD(p_IdSessao, 0, 'CODPARC'),0)
    -- , NVL(ACT_DEC_FIELD(p_IdSessao, 0, 'IDALIQ'),0)
    , NVL(ACT_DEC_FIELD(p_IdSessao, 0, 'CODPROD'),0)

    INTO
    V_VLRICMS, V_NUNOTA, V_SEQUENCIA, V_CODPARC, V_CODPROD
    --VLRUNIT, VLRICMS, VLRSUBST, VLRIPI, NUNOTA, IDALIQ, CODPROD, CODPROD, CODPARC,
    --VLRUNIT, ICMSPRO_VLRDIFALDEST, ICMSPRO_VLRDIFALREM, ICMSPRO_VLRFCP, IPIPRO_PERCIPI, QTDNEG
    -- VLRUNIT, QTDNEG

    FROM DUAL;

    SELECT
        NVL(TPP.CODTAB,0)
    INTO V_CODTAB
    FROM
        TGFTPP TPP
        INNER JOIN TGFPAR PAR ON TPP.CODTIPPARC = PAR.CODTIPPARC
    WHERE
        PAR.CODPARC = V_CODPARC;
    -- V_NUTAB := NVL(ACT_DEC_FIELD(p_IdSessao, 0, 'NUTAB'),0);

    -- RAISE_APPLICATION_ERROR(-20001, 'V_NUNOTA -> ' || V_NUNOTA );
    -- PRECO := V_CODPARC;
    -- p_Result := '{"PRECO":"' || PRECO || '"}';
    -- RETURN;

    -- V_NUNOTA    := NVL(ACT_INT_FIELD(p_IdSessao, 0, 'NUNOTA'),0);
    -- V_CODPROD   := NVL(ACT_DEC_FIELD(p_IdSessao, 0, 'CODPROD'),0);
    -- V_SEQUENCIA := NVL(ACT_DEC_FIELD(p_IdSessao, 0, 'SEQUENCIA'),0);


    BEGIN
        SELECT SNK_PRECO(V_CODTAB, V_CODPROD)
            INTO PRECO
            FROM DUAL;
    EXCEPTION
        WHEN NO_DATA_FOUND THEN
            PRECO := 0;
            p_Result := '{"PRECO":"' || PRECO || '"}';
            RETURN;
    END;
    -- PRECO := V_CODPROD;
    -- p_Result := '{"PRECO":"' || PRECO || '"}';
    -- RETURN;


    BEGIN
        SELECT AD_PRECODINAMICO
        INTO V_UTILIZAPRECO
        FROM TGFCAB CAB
        INNER JOIN TGFTOP TOP ON CAB.CODTIPOPER = TOP.CODTIPOPER AND CAB.DHTIPOPER = TOP.DHALTER
        WHERE CAB.NUNOTA = V_NUNOTA;
    EXCEPTION
        WHEN NO_DATA_FOUND THEN
            p_Result := '{"PRECO":"' || PRECO || '"}';
            RETURN;
    END;



    IF V_UTILIZAPRECO = 'S' THEN

        BEGIN
            SELECT UFS.CODUF AS CODUFORIGEM, UFS2.CODUF AS CODUFDESTINO, PAR.CLASSIFICMS
            INTO V_CODUFORIGEM, V_CODUFDESTINO, V_CLASSIFICMS
            FROM TGFCAB CAB
            INNER JOIN TSIEMP EMP ON EMP.CODEMP = CAB.CODEMP
            INNER JOIN TSICID CID ON CID.CODCID = EMP.CODCID
            INNER JOIN TSIUFS UFS ON UFS.CODUF = CID.UF
            INNER JOIN TGFPAR PAR ON PAR.CODPARC = CAB.CODPARC
            INNER JOIN TSICID CID2 ON CID2.CODCID = PAR.CODCID
            INNER JOIN TSIUFS UFS2 ON UFS2.CODUF = CID2.UF
            WHERE CAB.NUNOTA = V_NUNOTA;
        EXCEPTION
            WHEN NO_DATA_FOUND THEN
            p_Result := '{"PRECO":"' || PRECO || '"}';
            RETURN;
        END;



        -- BUSCA DADOS FISCAIS DO PRODUTO
        BEGIN
    SELECT NVL(TIPSUBST, 'N'),
           NVL(ORIGPROD, '0')
    INTO V_TIPSUBST, V_ORIGPROD
    FROM TGFPRO
    WHERE CODPROD = V_CODPROD;
EXCEPTION
    WHEN NO_DATA_FOUND THEN
        p_Result := '{"PRECO":"' || PRECO || '"}';
        RETURN;
END;

         	   -- ORIGEM DO PRODUTO COM ALÍQUOTA FIXA 4%
        IF V_ORIGPROD IN ('1','2','3','8')
		   AND V_CODUFORIGEM <> V_CODUFDESTINO THEN

            PRECOFINAL := PRECO + (PRECO * (4 / 100));
            p_Result := '{"PRECO":"' || PRECOFINAL || '"}';
            RETURN;
        END IF;


		   -- MESMO ESTADO OU CONSUMIDOR FINAL + PRODUTO ST
        IF ((V_CODUFORIGEM = V_CODUFDESTINO OR V_CLASSIFICMS = 'C')
            AND V_TIPSUBST <> 'N') THEN
            p_Result := '{"PRECO":"' || PRECO || '"}';
            RETURN;
        END IF;



        IF V_CLASSIFICMS = 'C' THEN
            BEGIN
                SELECT ICM.ALIQUOTA
                INTO V_ALIQUOTA
                FROM TGFICM ICM
                WHERE ICM.UFORIG = V_CODUFORIGEM AND ICM.UFDEST = V_CODUFORIGEM
                AND TIPRESTRICAO = 'S'
                AND TIPRESTRICAO2 = 'S';

            EXCEPTION
                WHEN NO_DATA_FOUND THEN
                p_Result := '{"PRECO":"' || PRECO || '"}';
                RETURN;
            END;

            PRECOFINAL := PRECO + (PRECO * (V_ALIQUOTA / 100));
            p_Result := '{"PRECO":"' || PRECOFINAL || '"}';
            RETURN;
        END IF;

        -- ALIQUOTA EXTERNA
        BEGIN
            SELECT ICM.ALIQUOTA
            INTO V_ALIQUOTA
            FROM TGFICM ICM
            WHERE ICM.UFORIG = V_CODUFORIGEM AND ICM.UFDEST = V_CODUFDESTINO
            AND TIPRESTRICAO = 'S'
            AND TIPRESTRICAO2 = 'S';

        EXCEPTION
            WHEN NO_DATA_FOUND THEN
            p_Result := '{"PRECO":"' || PRECO || '"}';
            RETURN;
        END;

        PRECOFINAL := PRECO + (PRECO * (V_ALIQUOTA / 100));
        p_Result := '{"PRECO":"' || PRECOFINAL || '"}';
        RETURN;
    ELSE
        p_Result := '{' ||
                    '"PRECO": "' || PRECO || '"' ||
                    '}';
    END IF;

    RETURN;

EXCEPTION
    WHEN OTHERS THEN
        p_Result := '{"MSG_ERRO": "Erro: ' || REPLACE(SQLERRM, '"', '''') || '"}';
END;