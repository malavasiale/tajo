package mytests;

import static org.junit.Assert.*;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.apache.tajo.ExecutionBlockId;
import org.apache.tajo.TaskId;
import org.apache.tajo.conf.TajoConf;
import org.apache.tajo.master.event.TaskAttemptToSchedulerEvent;
import org.apache.tajo.util.JSPUtil;
import org.apache.tajo.util.TajoIdUtils;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import junitparams.JUnitParamsRunner;
import junitparams.Parameters;

import org.apache.hadoop.conf.Configuration;
import org.apache.tajo.querymaster.Task;



@RunWith(JUnitParamsRunner.class)
public class TaskComparatorTest {
	
	List<Task> toOrder;
	Configuration conf;
	TaskAttemptToSchedulerEvent.TaskAttemptScheduleContext scheduleContext;
	ExecutionBlockId ebId;
	
	@Before
	public void prepareTasks() {
		toOrder = new ArrayList<>();
	    conf = new TajoConf();

	    scheduleContext = new TaskAttemptToSchedulerEvent.TaskAttemptScheduleContext();

	    ebId = TajoIdUtils.createExecutionBlockId("eb_000001_00001_00001");
	}
	
	public Task createTask(int taskId,int launchTime,int finishTime) {
		TaskId id = new TaskId(ebId, taskId);
	    Task task = new Task(conf, scheduleContext, id, true, null);
	    
	    task.setLaunchTime(launchTime);
        task.setFinishTime(finishTime);
        
        return task;
	}
	
	/*
	 * ----------START TEST SORT TASK ---------------------*
	 * Test del sort tramite id del metodo sortTask()
	 * Category Partition:
	 * List<Task> tasks : {valid, empty , null}
	 * String order : {desc , asc , invalid, null}
	 * String field : {id , host , runTime, startTime, invalid, null}
	 * */
	@Test
	@Parameters({
		"3,1,2,desc,3", // tasks = valid ; order = desc ; field = id
		"3,1,2,asc,1", // tasks = valid ; order = asc ; field = id
		"3,1,2,,3" // tasks = valid ; order = invalid ; field = id
	})
	public void sortTaskIdTest(int task1,int task2,int task3,String order,int expected) {
		List<Integer> tasksID = Arrays.asList(task1,task2,task3);
		for(Integer i : tasksID) {
			Task t = createTask(i,1,2);
			toOrder.add(t);
		}
		JSPUtil.sortTasks(toOrder, "id", order);
		assertEquals(expected,toOrder.get(0).getId().getId());
	}
	
	@Test
	// tasks = empty ; order = desc ; field = id
	// tasks = null ;  order = desc ; field = id
	public void sortTaskIdInvalidTasks() {
		JSPUtil.sortTasks(toOrder, "id", "desc");
		assertEquals(0,toOrder.size());
		
		toOrder = null;
		boolean t = false;
		try {
			JSPUtil.sortTasks(toOrder, "id", "desc");
		} catch (NullPointerException e) {
			t = true;
		}
		assertTrue(t);
	}
	
	
	
	/*---------END TEST SORT TASK----------*/

}
