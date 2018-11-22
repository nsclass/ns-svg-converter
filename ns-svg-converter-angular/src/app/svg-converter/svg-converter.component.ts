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
import { Observable } from 'rxjs';
import { DomSanitizer, SafeHtml } from '@angular/platform-browser';
import { saveAs as importedSaveAs} from 'file-saver';

import { PickedFile } from '../drop-file-zone/picked-file';
import { SvgImageRespond } from './svg-image-respond.model';

@Component({
  selector: 'app-svg-converter',
  templateUrl: './svg-converter.component.html',
  styleUrls: ['./svg-converter.component.css']
})
export class SvgConverterComponent implements OnInit {
  pickedFile: PickedFile;
  loading: boolean;
  svgImageRespond: SvgImageRespond;
  svg: SafeHtml;

  constructor(private sanitizer: DomSanitizer) { }

  ngOnInit() {
    // this.svgConverter.subscribe(res => this.svgResponse = res,
    // error => this.error = error);
  }

  updateSvgImageRespond(svgImageRespond: SvgImageRespond) {
    this.svgImageRespond = svgImageRespond;
    this.svg = this.sanitizer.bypassSecurityTrustHtml(this.svgImageRespond.svgString);
  }

  updatePickedFile(pickedFile: PickedFile) {
    this.pickedFile = pickedFile;
    this.svgImageRespond = null;
  }

  saveAs(): void {
    if (this.svgImageRespond) {
      const blob = new Blob([this.svgImageRespond.svgString], { type: 'text/svg' });
      importedSaveAs(blob, this.svgImageRespond.filename + '.svg');
    }
  }

}
