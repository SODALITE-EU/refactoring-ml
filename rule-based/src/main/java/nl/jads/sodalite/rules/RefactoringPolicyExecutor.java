package nl.jads.sodalite.rules;

import nl.jads.sodalite.events.IEvent;
import org.drools.core.command.runtime.rule.FireAllRulesCommand;
import org.kie.api.command.Command;
import org.kie.api.runtime.StatelessKieSession;
import org.kie.internal.command.CommandFactory;

import javax.activation.DataHandler;
import java.util.ArrayList;
import java.util.List;

public class RefactoringPolicyExecutor extends DroolsRules {
    private RefactoringManager refactoringManager;

    public RefactoringPolicyExecutor(String ruleFile, String ruleDir) {
        super(ruleFile, ruleDir);
    }

    public RefactoringPolicyExecutor(DataHandler ruleFileBinary) {
        super(ruleFileBinary);
    }

    public RefactoringPolicyExecutor(String ruleFile, String ruleDir, RefactoringManager refactoringManager) {
        super(ruleFile, ruleDir);
        this.refactoringManager = refactoringManager;
    }

    public void cleanUp() {
    }

    public RuleExecutionResult insertEvent(List<IEvent> events) throws RulesException {
        StatelessKieSession knowledgeSession = kieContainer.newStatelessKieSession();
        List<Command> cmds = new ArrayList<Command>();
        for (IEvent e1 : events) {
            cmds.add(CommandFactory.newInsert(e1));
        }
        DisabledRuleSet disabledRuleSet = new DisabledRuleSet();
        cmds.add(CommandFactory.newSetGlobal("refMgt", refactoringManager));
        cmds.add(CommandFactory.newSetGlobal("disabledSet", disabledRuleSet));
        cmds.add(new FireAllRulesCommand(new DisabledRuleSetAgendaFilter(disabledRuleSet)));
        knowledgeSession.execute(CommandFactory.newBatchExecution(cmds));
        return null;
    }
}
