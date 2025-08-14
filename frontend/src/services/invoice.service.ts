import { Injectable } from '@angular/core';
import axios from 'axios';
import { BehaviorSubject, catchError, from, map, Observable, tap, throwError } from 'rxjs';
import { AxiosError, Invoice, UploadResponse } from '../models/invoice.interface';

const BASE_URL = 'http://localhost:8080/api/invoices';

@Injectable({ providedIn: 'root' })
export class InvoiceService {
  private invoicesSubject = new BehaviorSubject<Invoice[]>([]);
  public invoices$ = this.invoicesSubject.asObservable();

  private uploadErrorsSubject = new BehaviorSubject<string[]>([]);
  public uploadErrors$ = this.uploadErrorsSubject.asObservable();

  private uploadSuccessSubject = new BehaviorSubject<boolean>(false);
  public uploadSuccess$ = this.uploadSuccessSubject.asObservable();

  fetchInvoices(): void {
    from(axios.get<Invoice[]>(`${BASE_URL}`)).subscribe(res => {
      this.invoicesSubject.next(res.data);
    });
  }

  uploadCSV(file: File): Observable<UploadResponse> {
    const formData = new FormData();
    formData.append('file', file);

    // Reset state
    this.uploadErrorsSubject.next([]);
    this.uploadSuccessSubject.next(false);

    return from(
      axios.post<UploadResponse>(`${BASE_URL}/upload`, formData, {
        headers: { 'Content-Type': 'multipart/form-data' },
      })
    ).pipe(
      map(response => response.data),
      tap(() => {
        this.uploadSuccessSubject.next(true);
      }),
      catchError((err: AxiosError) => {
        console.error('Upload error:', err);
        const errors = err?.response?.data?.errors;
        if (errors && Array.isArray(errors)) {
          this.uploadErrorsSubject.next(errors);
        } else {
          this.uploadErrorsSubject.next(['Unknown upload error occurred.']);
        }
        this.uploadSuccessSubject.next(false);
        return throwError(() => err); 
      })
    );
  }
}
