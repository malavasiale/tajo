package mytests;

import static org.junit.Assert.*;

import java.util.ArrayList;
import java.util.List;

import org.apache.tajo.ExecutionBlockId;
import org.apache.tajo.TaskId;
import org.apache.tajo.conf.TajoConf;
import org.apache.tajo.master.event.TaskAttemptToSchedulerEvent;
import org.apache.tajo.util.JSPUtil;
import org.apache.tajo.util.TajoIdUtils;
import org.junit.Test;
import org.junit.runner.RunWith;

import junitparams.JUnitParamsRunner;
import junitparams.Parameters;

import org.apache.hadoop.conf.Configuration;
import org.apache.tajo.querymaster.Task;



@RunWith(JUnitParamsRunner.class)
public class TaskComparatorTest {
	
	
	@Test
	public void prepareTasks() {
		List<Task> toOrder = new ArrayList<>();
	    Configuration conf = new TajoConf();

	    TaskAttemptToSchedulerEvent.TaskAttemptScheduleContext scheduleContext =
	        new TaskAttemptToSchedulerEvent.TaskAttemptScheduleContext();

	    ExecutionBlockId ebId = TajoIdUtils.createExecutionBlockId("eb_000001_00001_00001");
	    
	    TaskId id = new TaskId(ebId, 1);
	    Task task = new Task(conf, scheduleContext, id, true, null);
	    
	    task.setLaunchTime(1);
        task.setFinishTime(3);
        
        toOrder.add(task);
        
        id = new TaskId(ebId, 3);
	    task = new Task(conf, scheduleContext, id, true, null);
	    
	    task.setLaunchTime(3);
        task.setFinishTime(5);
        
        toOrder.add(task);
        
        id = new TaskId(ebId, 2);
	    task = new Task(conf, scheduleContext, id, true, null);
	    
	    task.setLaunchTime(2);
        task.setFinishTime(4);
        
        toOrder.add(task);
        
        JSPUtil.sortTasks(toOrder, "id", "desc");
        assertEquals(3,toOrder.get(0).getId().getId());
    

	}

}
