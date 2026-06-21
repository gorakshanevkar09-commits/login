Feature: DB validation demo

  Scenario: Execute sequential transaction, valuation and collateral validation
    Given transaction, valuation and collateral data files exist
    When the sequential validation workflow executes
    Then the transaction step should complete successfully
    And the valuation step should complete successfully
    And the collateral step should complete successfully
    And the workflow notification should indicate success
    And database validation layers A, B and C should pass for all steps
    When the integration lambda is invoked
    Then the downstream integration should succeed
    When a validation report is generated
    And the report is emailed to recipients
    Then an email should be created for recipients
