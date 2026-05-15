import { Component, OnInit } from '@angular/core';
import { CommonModule, DatePipe } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { TableModule } from 'primeng/table';
import { ButtonModule } from 'primeng/button';
import { TagModule } from 'primeng/tag';
import { ToastModule } from 'primeng/toast';
import { ConfirmDialogModule } from 'primeng/confirmdialog';
import { DialogModule } from 'primeng/dialog';
import { InputTextModule } from 'primeng/inputtext';
import { SkeletonModule } from 'primeng/skeleton';
import { TooltipModule } from 'primeng/tooltip';
import { MessageService, ConfirmationService } from 'primeng/api';
import { PasskeyDto } from './passkey.dto';
import { PasskeyService } from './passkey.service';
import { RelativeTimePipe } from '../../pipes/relative-time.pipe';

@Component({
  selector: 'app-passkey-list',
  standalone: true,
  imports: [
    CommonModule, FormsModule, DatePipe,
    TableModule, ButtonModule, TagModule, ToastModule,
    ConfirmDialogModule, DialogModule, InputTextModule,
    SkeletonModule, TooltipModule,
    RelativeTimePipe
  ],
  providers: [MessageService, ConfirmationService],
  templateUrl: './passkey-list.component.html'
})
export class PasskeyListComponent implements OnInit {
  passkeys: PasskeyDto[] = [];
  loading = true;
  registering = false;
  saving = false;
  renameDialogVisible = false;
  editingName = '';
  editingKey: PasskeyDto | null = null;

  constructor(
    private passkeyService: PasskeyService,
    private messageService: MessageService,
    private confirmationService: ConfirmationService
  ) {}

  ngOnInit(): void { this.loadPasskeys(); }

  loadPasskeys(): void {
    this.loading = true;
    this.passkeyService.list().subscribe({
      next: keys => { this.passkeys = keys; this.loading = false; },
      error: () => { this.loading = false; this.showError('Failed to load passkeys'); }
    });
  }

  getDeviceIcon(transports: string[]): string {
    if (transports?.includes('internal')) return 'pi pi-mobile';
    if (transports?.includes('usb')) return 'pi pi-key';
    if (transports?.includes('hybrid')) return 'pi pi-qrcode';
    return 'pi pi-shield';
  }

  async registerNewPasskey(): Promise<void> {
    this.registering = true;
    try {
      await this.passkeyService.register();
      this.showSuccess('Passkey added successfully');
      this.loadPasskeys();
    } catch (e: any) {
      this.showError(e?.message || 'Registration failed or was cancelled');
    } finally {
      this.registering = false;
    }
  }

  openRenameDialog(key: PasskeyDto): void {
    this.editingKey = key;
    this.editingName = key.deviceName || '';
    this.renameDialogVisible = true;
  }

  saveRename(): void {
    if (!this.editingKey) return;
    this.saving = true;
    this.passkeyService.rename(this.editingKey.id, this.editingName).subscribe({
      next: updated => {
        const idx = this.passkeys.findIndex(k => k.id === updated.id);
        if (idx >= 0) this.passkeys[idx] = updated;
        this.renameDialogVisible = false;
        this.saving = false;
        this.showSuccess('Passkey renamed');
      },
      error: () => { this.saving = false; this.showError('Failed to rename passkey'); }
    });
  }

  confirmDelete(key: PasskeyDto): void {
    this.confirmationService.confirm({
      message: `Remove "${key.deviceName || 'this passkey'}"? You will no longer be able to use it to sign in.`,
      header: 'Remove passkey',
      icon: 'pi pi-exclamation-triangle',
      acceptButtonStyleClass: 'p-button-danger',
      accept: () => this.deletePasskey(key)
    });
  }

  deletePasskey(key: PasskeyDto): void {
    this.passkeyService.delete(key.id).subscribe({
      next: () => {
        this.passkeys = this.passkeys.filter(k => k.id !== key.id);
        this.showSuccess('Passkey removed');
      },
      error: () => this.showError('Failed to remove passkey')
    });
  }

  private showSuccess(msg: string): void {
    this.messageService.add({ severity: 'success', summary: 'Done', detail: msg, life: 3000 });
  }

  private showError(msg: string): void {
    this.messageService.add({ severity: 'error', summary: 'Error', detail: msg, life: 5000 });
  }
}
