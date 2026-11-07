# User Story: US-011 - Recursos de Crisis y Seguridad ante Riesgo Emocional
Feature: Consulta de líneas de emergencia y recursos de crisis para soporte del paciente

  Background:
    * url baseUrl + '/api/v1/crisis-resources'

  Scenario: Obtener los recursos y líneas de atención de crisis de salud mental
    When method get
    Then status 200
    And match response == '#[2]'
    And match response contains deep { phone: '113' }
    And match response[0].name contains 'Línea 113'
