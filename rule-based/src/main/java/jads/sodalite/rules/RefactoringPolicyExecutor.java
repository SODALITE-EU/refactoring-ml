package jads.sodalite.rules;

import org.drools.core.command.runtime.rule.FireAllRulesCommand;
import org.kie.api.command.Command;
import org.kie.api.runtime.StatelessKieSession;
import org.kie.internal.command.CommandFactory;

import javax.activation.DataHandler;
import java.util.ArrayList;
import java.util.List;

public class RefactoringPolicyExecutor extends DroolsRules {

    public RefactoringPolicyExecutor(String ruleFile, String ruleDir) {
        super(ruleFile, ruleDir);
    }

    public RefactoringPolicyExecutor(DataHandler ruleFileBinary) {
        super(ruleFileBinary);
    }

    public void cleanUp() {
    }

    public RuleExecutionResult insertEvent(IEvent event) throws RulesException {
        StatelessKieSession knowledgeSession = kieContainer.newStatelessKieSession();
        List<Command> cmds = new ArrayList<Command>();

        EventCollection ec = (EventCollection) event;
        for (IEvent e1 : ec.getiEvents()) {
            cmds.add(CommandFactory.newInsert(e1));
        }
        RefactoringManager refactoringManager =
                new RefactoringManager();
        cmds.add(CommandFactory.newSetGlobal("cusMgt", refactoringManager));
        cmds.add(CommandFactory.newSetGlobal("disabledSet", ec.getDisabledRuleSet()));
        cmds.add(new FireAllRulesCommand(new DisabledRuleSetAgendaFilter(ec.getDisabledRuleSet())));
        knowledgeSession.execute(CommandFactory.newBatchExecution(cmds));
        return null;
    }
}
