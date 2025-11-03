export interface Tenant {
  id: string;
  company_name: string;
  subdomain: string;
  operator_limit: number;
  created_at: string;
  is_active?: boolean;
  email?: string;
}

export interface CreateTenantPayload {
  company_name: string;
  email: string;
  operator_limit?: number;
}

export interface UpdateTenantPayload {
  company_name?: string;
  operator_limit?: number;
}

export interface TenantValidation {
  id: string;
  name: string;
  subdomain: string;
  active: boolean;
}
