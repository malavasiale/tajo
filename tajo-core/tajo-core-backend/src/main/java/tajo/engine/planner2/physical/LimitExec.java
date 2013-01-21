/*
 * Copyright 2012 Database Lab., Korea Univ.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package tajo.engine.planner2.physical;

import tajo.TaskAttemptContext2;
import tajo.catalog.Schema;
import tajo.engine.planner.logical.LimitNode;
import tajo.storage.Tuple;

import java.io.IOException;

public class LimitExec extends UnaryPhysicalExec {
  private final long fetchFirstNum;
  private long fetchCount;

  public LimitExec(TaskAttemptContext2 context, Schema inSchema,
                   Schema outSchema, PhysicalExec child, LimitNode limit) {
    super(context, inSchema, outSchema, child);
    this.fetchFirstNum = limit.getFetchFirstNum();
    this.fetchCount = 0;
  }

  @Override
  public Tuple next() throws IOException {
    Tuple tuple = child.next();
    fetchCount++;

    if (fetchCount > fetchFirstNum || tuple == null) {
      return null;
    }

    return tuple;
  }

  public void rescan() throws IOException {
    super.init();
    fetchCount = 0;
  }
}