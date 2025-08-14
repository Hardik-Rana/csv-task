export interface Invoice {
  id: number;
  customerId: string;
  invoiceNumber: string;
  invoiceDate: string; 
  description: string;
  amount: number;
  invoiceAge: number; 
}

export interface UploadResponse {
  status: 'success' | 'error';
  savedCount: number;
  errors?: string[];
  message?: string;
}

export interface AxiosError {
  response?: {
    data?: {
      errors?: string[];
      message?: string;
    };
  };
  message?: string;
}
