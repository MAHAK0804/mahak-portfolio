import { Routes } from '@angular/router';
import { HomeComponent } from './home';
import { PortfolioManagerComponent } from './components/portfolio-manager/portfolio-manager';

export const routes: Routes = [
	{ path: '', component: HomeComponent },
	{ path: 'manage', component: PortfolioManagerComponent },
	{ path: '**', redirectTo: '' }
];
