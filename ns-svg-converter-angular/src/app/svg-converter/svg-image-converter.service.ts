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

import { Injectable, Inject } from '@angular/core';
import {
  HttpClient,
  HttpRequest,
  HttpHeaders
} from '@angular/common/http';

import { Observable } from 'rxjs';
import { SvgImageRequest } from './svg-image-request.model';
import { SvgImageRespond } from './svg-image-respond.model';

export const SVG_CONVERTER_API_URL =
  '/api/v1/svg/conversion';

@Injectable()
export class SvgImageConverterService {
  constructor(
    private http: HttpClient,
    @Inject(SVG_CONVERTER_API_URL) private apiUrl: string
  ) {}

  convertImage(request: SvgImageRequest): Observable<SvgImageRespond> {
    const headers: HttpHeaders = new HttpHeaders({
      'Accept': 'application/json',
      'Content-Type': 'application/json'
    });

    const body = JSON.stringify(request);
    return this.http.put<SvgImageRespond>(this.apiUrl, body, {
      headers: headers
    });
  }
}
