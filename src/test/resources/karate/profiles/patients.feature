# User Story: US-006 - Edición del perfil del paciente y contacto de emergencia
Feature: Perfil del paciente autenticado y su contacto de emergencia

  Background:
    * def session = callonce read('classpath:karate/common/sign-up.feature')
    * configure headers = { Authorization: '#("Bearer " + session.token)' }
    * url baseUrl + '/api/v1/patients'

  Scenario: Consultar el propio perfil y actualizar el contacto de emergencia
    # 1. El registro en IAM crea el perfil del paciente (200)
    Given path 'me'
    When method get
    Then status 200
    And match response.email == session.email
    * def patientId = response.id

    # 2. Consultar el perfil por ID (200)
    Given path patientId
    When method get
    Then status 200
    And match response.id == patientId

    # 3. Actualizar contacto de emergencia (200)
    Given path patientId, 'emergency-contact'
    And request
    """
    {
      "name": "Ana Gomez",
      "relationship": "SPOUSE",
      "phoneNumber": "+51912345678",
      "email": "ana.gomez@example.com"
    }
    """
    When method put
    Then status 200
    And match response.emergencyContact.name == 'Ana Gomez'
    And match response.emergencyContact.relationship == 'SPOUSE'

  Scenario: Otra cuenta no puede leer el perfil de un paciente
    Given path 'me'
    When method get
    Then status 200
    * def patientId = response.id
    * def other = call read('classpath:karate/common/sign-up.feature')
    * configure headers = { Authorization: '#("Bearer " + other.token)' }
    Given path patientId
    When method get
    Then status 403

  Scenario: No se puede listar a todos los pacientes
    When method get
    Then status 403

  Scenario: Consultar un paciente inexistente retorna estado 404
    Given path 999999
    When method get
    Then status 404
