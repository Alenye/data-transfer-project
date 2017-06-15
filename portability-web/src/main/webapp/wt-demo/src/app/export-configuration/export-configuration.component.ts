import { Component, OnInit } from '@angular/core';
import { Router, ActivatedRoute, Params } from '@angular/router';
import { SelectDataTypeService } from '../select-data-type.service';
import { ServiceDescription, ServiceDescriptions } from '../service-description';
import 'rxjs/add/operator/switchMap';

@Component({
  templateUrl: './export-configuration.component.html',
  styleUrls: ['./export-configuration.component.css']
})

export class ExportConfigurationComponent implements OnInit {
  exportServices: ServiceDescription[] = [];
  selectedExportService: string = "";
  error_text: string = "";

  constructor(private service : SelectDataTypeService,
    private route: ActivatedRoute,
    private router: Router,) { }

  ngOnInit() {
    console.log('incoming route param, dataType: ' + this.route.params['type']);

    this.route.params
      .switchMap((params: Params) => this.service.listServices(params['type']))
      .subscribe(
        data => {
          this.exportServices = data.exportServices;
          this.selectedExportService = data.exportServices[0].name;
          console.log('setting exportServices: ' + JSON.stringify(this.exportServices));
        },
        error => {
          this.error_text = 'There was an error';
          console.error(error);
        }
      );
  }

  // Handles selection of data types
  onSelect(exportService: string) {
    console.log('incoming exportService: ' + exportService);
    // TODO: Send to auth
    this.router.navigate(['/import-configuration', exportService]);
  }
}