# User Story: US-011 - Recursos de Crisis y Seguridad ante Riesgo Emocional
Feature: Consulta de líneas de emergencia y recursos de crisis para soporte del paciente

  Background:
    * def session = callonce read('classpath:karate/common/sign-up.feature')
    * configure headers = { Authorization: '#("Bearer " + session.token)' }
    * url baseUrl + '/api/v1/crisis-resources'

  Scenario: Obtener los recursos y líneas de atención de crisis de salud mental
    When method get
    Then status 200
    And match response == '#[2]'
    And match response contains deep { phone: '113' }
    And match response[0].name contains 'Línea 113'

  Scenario: Una cuenta nueva no ve conversaciones ajenas
    Given url baseUrl + '/api/v1/assistant/conversations'
    When method get
    Then status 200
    And match response == '#[0]'

  Scenario: No se puede pedir el historial de conversaciones de otra cuenta
    Given url baseUrl + '/api/v1/conversation-sessions'
    And param accountId = session.accountId + 1000
    When method get
    Then status 403
