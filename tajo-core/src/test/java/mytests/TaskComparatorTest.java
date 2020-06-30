package mytests;

import static org.junit.Assert.*;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import org.apache.tajo.ExecutionBlockId;
import org.apache.tajo.TaskId;
import org.apache.tajo.conf.TajoConf;
import org.apache.tajo.master.cluster.WorkerConnectionInfo;
import org.apache.tajo.master.event.TaskAttemptToSchedulerEvent;
import org.apache.tajo.util.JSPUtil;
import org.apache.tajo.util.TajoIdUtils;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mockito;

import junitparams.JUnitParamsRunner;
import junitparams.Parameters;

import org.apache.hadoop.conf.Configuration;
import org.apache.tajo.querymaster.Task;


/*
 * MUTATION 209 & MUTATION 218 : non possono essere uccise in quanto equivalenti al SUT. In entrambe viene sostituito
 * il valore di ritorno 1 con 0. La classe Collections.sort() però utilizza il valore del comparatore passatogli
 * verificando se il suo valore è >=0 oppure <0. Quindi passandogli 0 invece che 1 segue esattamente lo stesso flusso 
 * di esecuzione dando lo stesso risultato. ----> WEAK MUTATION
 * */
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
	
	
	@Test
	//tasks = valid ; order = asc ; field = empty
	// tasks = valid ; order = desc ; field = null
	// tasks = valid ; order = desc ; field = invalid
	// tasks = valid ; order = asc ; field = invalid FOR COVERAGE
	public void sortTaskInvalidField() {
		toOrder.add(createTask(1,1,2));
		toOrder.add(createTask(3,2,3));
		toOrder.add(createTask(2,1,2));
		JSPUtil.sortTasks(toOrder, "", "asc");
		assertEquals(1,toOrder.get(0).getId().getId());
		
		Collections.shuffle(toOrder);
		JSPUtil.sortTasks(toOrder, null, "desc");
		assertEquals(3,toOrder.get(0).getId().getId());
		
		Collections.shuffle(toOrder);
		JSPUtil.sortTasks(toOrder, "invalid", "desc");
		assertEquals(3,toOrder.get(0).getId().getId());
		
		Collections.shuffle(toOrder);
		JSPUtil.sortTasks(toOrder, "invalid", "asc");
		assertEquals(1,toOrder.get(0).getId().getId());
	}
	
	@Test
	//tasks = valid ; order = asc ; field = host
	//tasks = valid ; order = desc ; field = host
	public void sortTaskHostTest() {
		Task t1 = createTask(1,1,2);
		Task t2 = createTask(2,2,3);
		toOrder.add(t2);
		toOrder.add(t1);
		JSPUtil.sortTasks(toOrder, "host", "asc");
		assertEquals(2,toOrder.get(0).getId().getId());
		
		JSPUtil.sortTasks(toOrder, "host", "desc");
		assertEquals(2,toOrder.get(0).getId().getId());
	}
	
	
	
	/*
	 * In  questo test viene eseguito un mock sull assegnazione degli host ai vari task per testare
	 * la funzionalità di TaskComparator quando gli host sono diversi dal valore null
	 * */
	@Test
	//tasks = valid ; order = desc ; field = host FOR COVERAGE
	//tasks = valid ; order = asc ; field = host FOR COVERAGE
	public void sortTaskHostMocked() {
		Task task1 = createTask(1,1,2);
		Task task2 = createTask(2,1,2);
		task1 = Mockito.spy(task1);
		task2 = Mockito.spy(task2);
		WorkerConnectionInfo conn1 = Mockito.mock(WorkerConnectionInfo.class);
		WorkerConnectionInfo conn2 = Mockito.mock(WorkerConnectionInfo.class);
		
		Mockito.doReturn(conn1).when(task1).getSucceededWorker();
		Mockito.doReturn(conn2).when(task2).getSucceededWorker();
		

		Mockito.doReturn("hostA").when(conn1).getHost();
		Mockito.doReturn("hostB").when(conn2).getHost();
		
		toOrder.add(task1);
		toOrder.add(task2);
		JSPUtil.sortTasks(toOrder, "host", "desc");
		assertEquals(2,toOrder.get(0).getId().getId());
		
		JSPUtil.sortTasks(toOrder, "host", "asc");
		assertEquals(1,toOrder.get(0).getId().getId());
		
	}
	
	@Test
	@Parameters({
		"2,5,1,desc,2", //tasks = valid ; order = desc ; field = runTime
		"2,5,1,asc,3" //tasks = valid ; order = desc ; field = runTime
	})
	public void sortTaskRunTimeTest(int runTime1,int runTime2, int runTime3,String order,int expected) {
		toOrder.add(createTask(1,1,1+runTime1));
		toOrder.add(createTask(2,1,1+runTime2));
		toOrder.add(createTask(3,1,1+runTime3));
		JSPUtil.sortTasks(toOrder, "runTime", order);
		assertEquals(expected,toOrder.get(0).getId().getId());
		
	}
	
	@Test
	//tasks = valid ; order = desc ; field = runTime FOR COVERAGE
	//tasks = valid ; order = desc ; field = runTime FOR COVERAGE
	public void sortTaskNullRunTime() {
		toOrder.add(createTask(2,0,2));
		toOrder.add(createTask(1,1,2));
		JSPUtil.sortTasks(toOrder, "runTime", "desc");
		assertEquals(1,toOrder.get(0).getId().getId());
		
		toOrder.clear();
		toOrder.add(createTask(1,1,2));
		toOrder.add(createTask(2,0,2));
		JSPUtil.sortTasks(toOrder, "runTime", "desc");
		assertEquals(1,toOrder.get(0).getId().getId());
	}
	
	@Test
	@Parameters({
		"2,5,1,desc,2", //tasks = valid ; order = desc ; field = startTime
		"2,5,1,asc,3" //tasks = valid ; order = desc ; field = startTime
	})
	public void sortTaskStartTimeTest(int startTime1,int startTime2, int startTime3,String order,int expected) {
		toOrder.add(createTask(1,startTime1,1));
		toOrder.add(createTask(2,startTime2,1));
		toOrder.add(createTask(3,startTime3,1));
		JSPUtil.sortTasks(toOrder, "startTime", order);
		assertEquals(expected,toOrder.get(0).getId().getId());
		
	}
	
	
	
	/*---------END TEST SORT TASK----------*/

}
