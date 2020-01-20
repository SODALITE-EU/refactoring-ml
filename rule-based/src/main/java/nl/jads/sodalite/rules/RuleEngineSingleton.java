package nl.jads.sodalite.rules;

public class RuleEngineSingleton {
    private static RuleEngineSingleton instance;
    private static RefactoringPolicyExecutor policyExecutor;

    static {
        try {
            instance = new RuleEngineSingleton();
        } catch (Exception e) {
            throw new RuntimeException("Exception occurred in creating singleton instance");
        }
    }

    private RuleEngineSingleton() {
        policyExecutor = new RefactoringPolicyExecutor("refactoring.drl", "rules");
    }

    public static RuleEngineSingleton getInstance() {
        return instance;
    }

    public static RefactoringPolicyExecutor getPolicyExecutor() {
        return policyExecutor;
    }
}
