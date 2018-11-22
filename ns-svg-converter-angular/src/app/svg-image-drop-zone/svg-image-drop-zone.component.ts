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
  Output,
  ViewChild,
  EventEmitter
} from '@angular/core';

import { ReadMode } from '../drop-file-zone';
import { FilePickerDirective, PickedFile } from '../drop-file-zone';

@Component({
  selector: 'app-svg-image-drop-zone',
  templateUrl: './svg-image-drop-zone.component.html',
  styleUrls: ['./svg-image-drop-zone.component.css']
})
export class SvgImageDropZoneComponent implements OnInit {

  public readMode = ReadMode.dataURL;
  public isHover: boolean;

  public picked: PickedFile;
  public status: string;

  @ViewChild(FilePickerDirective)
  private filePicker;

  @Output() pickedFileResult: EventEmitter<PickedFile> = new EventEmitter<PickedFile>();

  constructor() { }

  addFile(file: PickedFile) {
    this.picked = file;
    this.pickedFileResult.emit(file);
  }

  ngOnInit() {
  }

  onReadStart(fileCount: number) {
    this.status = `Reading ${fileCount} file(s).`;
  }

  onFilePicked(file: PickedFile) {
    this.picked = file;
    this.pickedFileResult.emit(file);
  }

  onReadEnd(fileCount: number) {
    this.status = `Read ${fileCount} file(s) on ${new Date().toLocaleTimeString()}.`;
    this.filePicker.reset();
  }
}
