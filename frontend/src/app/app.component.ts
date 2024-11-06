import { Component } from '@angular/core';
import { RouterOutlet } from '@angular/router';
import {TestifyComponent} from './testify/testify.component';

@Component({
  selector: 'app-root',
  standalone: true,
  imports: [RouterOutlet, TestifyComponent],
  templateUrl: './app.component.html',
  styleUrl: './app.component.scss'
})
export class AppComponent {
  title = 'frontend';
}
