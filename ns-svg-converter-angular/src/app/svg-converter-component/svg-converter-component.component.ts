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

import {
  Component,
  OnInit,
  Input,
  Output,
  EventEmitter
 } from '@angular/core';

import { SvgImageConverterService } from '../svg-converter/svg-image-converter.service';
import { SvgImageRequest } from '../svg-converter/svg-image-request.model';
import { SvgImageRespond } from '../svg-converter/svg-image-respond.model';
import { PickedFile } from '../drop-file-zone/picked-file';

import { Observable } from 'rxjs';

@Component({
  selector: 'app-svg-converter-component',
  templateUrl: './svg-converter-component.component.html',
  styleUrls: ['./svg-converter-component.component.css']
})
export class SvgConverterComponentComponent implements OnInit {
  @Input() pickedFile: PickedFile;
  @Output() loading: EventEmitter<boolean> = new EventEmitter<boolean>();
  @Output() svgImageResult: EventEmitter<SvgImageRespond> = new EventEmitter<SvgImageRespond>();

  errorMsg: string;

  constructor(private svgConverter: SvgImageConverterService) { }

  ngOnInit() {
  }

  convertImage(): void {
    this.errorMsg = null;
    this.svgImageResult.emit(null);
    this.loading.emit(true);
    const request: SvgImageRequest = new SvgImageRequest(this.pickedFile.name, this.pickedFile.content, 16);
    this.svgConverter
    .convertImage(request)
    .subscribe(res => {
      console.log(res);
      this.loading.emit(false);
      this.svgImageResult.emit(res);
    }, err => {
      this.loading.emit(false);
      const splitRes = err.error.message.split(':');
      if (splitRes.length > 1 ) {
        this.errorMsg = splitRes[1];
      } else {
        this.errorMsg = err.error.message;
      }
      console.log(err);
    }, () => {
      this.loading.emit(false);
    });
  }
}
