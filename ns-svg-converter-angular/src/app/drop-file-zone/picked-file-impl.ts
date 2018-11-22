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

import { PickedFile } from './picked-file';
import { ReadMode } from './read-mode.enum';

export class PickedFileImpl implements PickedFile {
  get lastModifiedDate(): Date {
    return this._lastModifiedDate;
  }

  get name(): string {
    return this._name;
  }

  get size(): number {
    return this._size;
  }

  get type(): string {
    return this._type;
  }

  get readMode(): ReadMode {
    return this._readMode;
  }

  get content(): any {
    return this._content;
  }

  constructor(
    private _lastModifiedDate: Date,
    private _name: string,
    private _size: number,
    private _type: string,
    private _readMode: ReadMode,
    private _content: any) {
  }
}
