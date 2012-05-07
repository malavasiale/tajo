/**
 * 
 */
package nta.engine.planner.physical;

import java.io.IOException;

import nta.catalog.Schema;
import nta.engine.query.exception.InvalidQueryException;
import nta.storage.Tuple;

/**
 * @author Hyunsik Choi
 *
 */
public class UnionExec extends PhysicalExec {
  private final Schema schema;
  private boolean nextOuter = true;
  private final PhysicalExec outer;
  private final PhysicalExec inner;
  private Tuple tuple;

  public UnionExec(PhysicalExec outer, PhysicalExec inner) {    
    if (!outer.getSchema().equals(inner.getSchema())) {
      throw new InvalidQueryException(
          "The schemas of both operators are not matched");
    }    
    schema = outer.getSchema();
    this.outer = outer;
    this.inner = inner;
  }

  @Override
  public Schema getSchema() {
    return schema;
  }

  @Override
  public Tuple next() throws IOException {
    if (nextOuter == true) {
      tuple = outer.next();
      if (tuple == null) {
       nextOuter = false; 
      } else {
        return tuple;
      }
    }
    
    return inner.next();    
  }

  @Override
  public void rescan() throws IOException {
    outer.rescan();
    inner.rescan();
    nextOuter = true;
  }
}