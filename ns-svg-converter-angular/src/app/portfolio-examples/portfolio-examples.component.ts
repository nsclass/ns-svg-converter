/*
 * Copyright 2017-present, Nam Seob Seo
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

import { Component, OnInit } from '@angular/core';

@Component({
  selector: 'app-portfolio-examples',
  templateUrl: './portfolio-examples.component.html',
  styleUrls: ['./portfolio-examples.component.css']
})
export class PortfolioExamplesComponent implements OnInit {

  public sampleMap = new Array();

  constructor() {
    this.sampleMap.push(['assets/images/samples/SVG_logo.png',
    'assets/images/samples/SVG_logo.svg', 'SVG logo']);
    this.sampleMap.push(['assets/images/samples/spider.jpg',
    'assets/images/samples/spider.jpg.svg', 'Spider']);
  }

  ngOnInit() {
  }

}
