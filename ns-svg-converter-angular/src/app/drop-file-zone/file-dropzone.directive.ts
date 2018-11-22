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

import { Directive, EventEmitter, HostListener, Input, Output } from '@angular/core';

import { PickedFile } from './picked-file';
import { PickedFileImpl } from './picked-file-impl';
import { ReadMode } from './read-mode.enum';

@Directive({
  selector: '[appFileDropzone]'
})
export class FileDropzoneDirective {
  @Input('appFileDropzone') readMode: ReadMode;

  @Output()
  public fileDrop = new EventEmitter<PickedFile>();

  @HostListener('dragenter', ['$event'])
  public onDragEnter(event) {
    event.stopPropagation();
    event.preventDefault();
  }

  @HostListener('dragover', ['$event'])
  public onDragOver(event) {
    event.stopPropagation();
    event.preventDefault();
  }

  @HostListener('drop', ['$event'])
  public onDrop(event) {
    event.stopPropagation();
    event.preventDefault();

    const dt = event.dataTransfer;
    const files = dt.files;

    for (let i = 0; i < files.length; i++) {
      this.readFile(files[i]);
    }
  }

  private readFile(file: File) {
    const reader = new FileReader();

    reader.onload = (loaded: ProgressEvent) => {
      const fileReader = loaded.target as FileReader;
      const droppedFile = new PickedFileImpl(file.lastModifiedDate, file.name, file.size, file.type, this.readMode, fileReader.result);

      this.fileDrop.emit(droppedFile);
    };

    switch (this.readMode) {
      case ReadMode.arrayBuffer:
        reader.readAsArrayBuffer(file);
        break;
      case ReadMode.binaryString:
        reader.readAsBinaryString(file);
        break;
      case ReadMode.text:
        reader.readAsText(file);
        break;
      case ReadMode.dataURL:
      default:
        reader.readAsDataURL(file);
        break;
    }
  }
}
