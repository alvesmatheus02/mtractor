CREATE OR REPLACE PROCEDURE STP_CALCULA_PRECO_DINAMICO(
    p_IdSessao VARCHAR2,
    p_Result OUT VARCHAR2
)
AS
    PRECO                 FLOAT := 0;
    V_CODPROD             NUMBER;
    V_CODPARC             NUMBER;

BEGIN

    SELECT
		  NVL(ACT_DEC_FIELD(p_IdSessao, 0, 'CODPARC'),0)
		, NVL(ACT_DEC_FIELD(p_IdSessao, 0, 'CODPROD'),0)
    INTO
		V_CODPARC, V_CODPROD
    FROM DUAL;

    SELECT
		NVL(BHZ_FC_CALCULA_PRECO_DINAMICO (V_CODPARC,V_CODPROD,(SELECT NVL(CODEMP,1) FROM TSIUSU WHERE CODUSU = STP_GET_CODUSULOGADO),1),0)
	INTO
		PRECO
	FROM DUAL;

        p_Result := '{' ||
                    '"PRECO": "' || PRECO || '"' ||
                    '}';

    RETURN;

EXCEPTION
    WHEN OTHERS THEN
        p_Result := '{"MSG_ERRO": "Erro: ' || REPLACE(SQLERRM, '"', '''') || '"}';
END;