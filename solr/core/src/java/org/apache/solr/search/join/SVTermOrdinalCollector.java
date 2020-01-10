/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.apache.solr.search.join;

import java.io.IOException;
import java.lang.invoke.MethodHandles;

import org.apache.lucene.index.LeafReaderContext;
import org.apache.lucene.index.SortedDocValues;
import org.apache.lucene.search.ScoreMode;
import org.apache.lucene.util.LongBitSet;
import org.apache.solr.search.DelegatingCollector;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Populates a bitset of (top-level) ordinals based on field values in a single-valued field.
 */
public class SVTermOrdinalCollector extends DelegatingCollector {
  private static final Logger log = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());

  private int docBase;
  private SortedDocValues topLevelDocValues;
  private final String fieldName;
  private final LongBitSet topLevelDocValuesBitSet;

  public SVTermOrdinalCollector(String fieldName, SortedDocValues topLevelDocValues, LongBitSet topLevelDocValuesBitSet) {
    this.fieldName = fieldName;
    this.topLevelDocValues = topLevelDocValues;
    this.topLevelDocValuesBitSet = topLevelDocValuesBitSet;
  }

  public ScoreMode scoreMode() {
    return ScoreMode.COMPLETE_NO_SCORES;
  }

  public boolean needsScores(){
    return false;
  }

  @Override
  public void doSetNextReader(LeafReaderContext context) throws IOException {
    this.docBase = context.docBase;
  }

  @Override
  public void collect(int doc) throws IOException {
    final int globalDoc = docBase + doc;

    if (topLevelDocValues.advanceExact(globalDoc)) {
      topLevelDocValuesBitSet.set(topLevelDocValues.ordValue());
    }
  }
}
