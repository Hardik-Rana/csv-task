import { CommonModule } from '@angular/common';
import { Component, OnInit, signal } from '@angular/core';
import { Observable } from 'rxjs';
import { Invoice } from '../../../models/invoice.interface';
import { InvoiceService } from '../../../services/invoice.service';

@Component({
  selector: 'app-home',
  standalone: true,
  imports: [CommonModule],
  templateUrl: './home.html',
  styleUrl: './home.css'
})
export class Home implements OnInit {
  invoices$!: Observable<Invoice[]>;
  uploadErrors$!: Observable<string[]>;
  uploadSuccess$!: Observable<boolean>;

  fileToUpload: File | null = null;

  // Pagination state
  currentPage = signal(1);
  itemsPerPage = 10;

  constructor(private invoiceService: InvoiceService) {}

  ngOnInit(): void {
    this.invoices$ = this.invoiceService.invoices$;
    this.uploadErrors$ = this.invoiceService.uploadErrors$;
    this.uploadSuccess$ = this.invoiceService.uploadSuccess$;

    this.invoiceService.fetchInvoices();
  }

  handleFileChange(event: Event) {
    const target = event.target as HTMLInputElement;
    if (target.files?.length) {
      this.fileToUpload = target.files[0];
    }
  }

  upload() {
    if (!this.fileToUpload) return;

    this.invoiceService.uploadCSV(this.fileToUpload).subscribe({
      next: () => {
        this.invoiceService.fetchInvoices();
        this.fileToUpload = null;
      },
      error: () => {
        this.fileToUpload = null;
      }
    });
  }

  getInvoiceAge(invoiceDate: string): number {
    const today = new Date();
    const date = new Date(invoiceDate);
    const diffTime = Math.abs(today.getTime() - date.getTime());
    return Math.floor(diffTime / (1000 * 60 * 60 * 24));
  }

  // Pagination helper
  paginatedInvoices(invoices: Invoice[]): Invoice[] {
    const start = (this.currentPage() - 1) * this.itemsPerPage;
    return invoices.slice(start, start + this.itemsPerPage);
  }

  totalPages(invoices: Invoice[]): number {
    return Math.ceil(invoices.length / this.itemsPerPage);
  }

  setPage(page: number) {
    this.currentPage.set(page);
  }
}

export default Home;
