       IDENTIFICATION DIVISION.
       PROGRAM-ID. CLAIMVAL.
       AUTHOR. FLORENCIA SOMBRA.

       ENVIRONMENT DIVISION.

       DATA DIVISION.
       WORKING-STORAGE SECTION.
       01 ws-mensaje pic x(45) 
           value 'Sistema de auditoria de reclamos iniciado'.

      *estructura de reclamo (simulando el json de la api)
       01 ws-claim-record.
         05 ws-policy-id     pic x(10) value 'POL001001'.
         05 ws-customer-id   pic x(10) value 'cus00001  '.
         05 ws-claim-amount  pic 9(5)v99 value 50000.00.
         05 ws-user-id       pic x(10) value 'OPER001.  '.
         05 ws-user-role     pic x(15) value 'CLAIM_OPERATOR'.
         05 ws-claim-status  pic x(15) VALUE 'pending      '.
         05 ws-reason-code   pic x(40) value SPACES.

      * mock del area de comuncación sql (SQLCA)
       01 ws-sqlca-mock.
           05 ws-sqlcode     pic s9(9) value 0.

       procedure division.
       000-main-process.
           PERFORM 100-INITIALIZE.
           PERFORM 200-EVALUATE-FRAUD.
           PERFORM 300-PRINT-RESULT.
           PERFORM 400-DB2-INSERT-MOCK.
       
      * Se envia la alerta a MQ si está en revisión
           IF WS-CLAIM-STATUS = 'UNDER_REVIEW'
              PERFORM 500-PUBLISH-MQ-EVENT
           END-IF.

           STOP RUN.

       100-INITIALIZE.
           DISPLAY '---------------------------------'.
           DISPLAY WS-MENSAJE.
           DISPLAY '---------------------------------'.
       
       200-EVALUATE-FRAUD.
           IF WS-CLAIM-AMOUNT > 10000 and 
           WS-USER-ROLE = 'CLAIM_OPERATOR'
              MOVE 'UNDER_REVIEW' TO WS-CLAIM-STATUS
              MOVE 'HIGH_VALUE_REQUIRES_SUPERVISOR' TO WS-REASON-CODE
           ELSE
              MOVE 'APROVED' TO WS-CLAIM-STATUS
              MOVE 'AUTO_PROCESSED' TO WS-REASON-CODE
           END-IF.
       
       300-PRINT-RESULT.
           DISPLAY 'EVALUANDO RECLAMO DE POLIZA: ' WS-POLICY-ID.
           DISPLAY 'USUARIO OPERADOR: ' WS-USER-ID 'ROL: ' WS-USER-ROLE.
           DISPLAY 'MONTO DEL RECLAMO: $' WS-CLAIM-AMOUNT.
           DISPLAY '>> ESTADO FINAL: ' WS-CLAIM-STATUS
           DISPLAY '>> MOTIVO: ' WS-REASON-CODE
           DISPLAY '---------------------------------'.

       400-DB2-INSERT-MOCK.
           DISPLAY 'INICIANDO TRANSACCION DB2...'.
           DISPLAY 'EXEC SQL INSERT INTO CLAIMS... END-EXEC(SIMULADO)'.
      * Simulamos que la base de datos insertó el registro con éxito
           MOVE 0 TO WS-SQLCODE.

           IF WS-SQLCODE = 0
              DISPLAY 'SQLCODE: ' WS-SQLCODE 
              ' -> EXEC SQL COMMIT END-EXEC'
              DISPLAY 'INTEGRIDAD CONFIRMADA: REGISTRO GUARDADO.'
           ELSE
              DISPLAY 'ERROR CRITICO. SQLCODE: ' WS-SQLCODE
              DISPLAY '-> EXEC SQL ROLLBACK END-EXEC.'
              DISPLAY 'INTEGRIDAD PROTEGIDA: CAMBIOS REVERTIDOS'
           END-IF.
           DISPLAY '---------------------------------'.

       500-PUBLISH-MQ-EVENT.
           DISPLAY '>>> INYECTANDO EVENTO EN COLA IBM MQ (SIMULADO) <<<'.
           DISPLAY 'QUEUE: INSURANCE.FRAUD.ALERTS'.
           DISPLAY '{'.
           DISPLAY '  "eventType": "CLAIM_UNDER_REVIEW", '.
           DISPLAY '  "policyId": "' WS-POLICY-ID '",'.
           DISPLAY '  "claimAmount":  ' WS-CLAIM-AMOUNT ','.
           DISPLAY '  "reason": "' WS-REASON-CODE '"'.
           DISPLAY '}'.
           DISPLAY '>>> MENSAJE ENVIADO CON EXITO <<<'.
           DISPLAY '---------------------------------'.
